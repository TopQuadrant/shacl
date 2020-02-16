/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
package org.topbraid.shacl.validation.sparql;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.jenax.statistics.ExecStatistics;
import org.topbraid.jenax.statistics.ExecStatisticsManager;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.jenax.util.RDFLabels;
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.arq.functions.HasShapeFunction;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.util.FailureLog;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.validation.ConstraintExecutor;
import org.topbraid.shacl.validation.SHACLException;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

public abstract class AbstractSPARQLExecutor implements ConstraintExecutor {
	
	// Flag to generate dash:SuccessResults for all violations.
	public static boolean createSuccessResults = false;
	
	private Query query;
	
	private String queryString;
	
	
	protected AbstractSPARQLExecutor(Constraint constraint) {
		this.queryString = getSPARQL(constraint);
		try {
			this.query = ARQFactory.get().createQuery(queryString);
			Resource path = constraint.getShapeResource().getPath();
			if(path != null && path.isAnon()) {
				String pathString = SHACLPaths.getPathString(constraint.getShapeResource().getPropertyResourceValue(SH.path));
				query = SPARQLSubstitutions.substitutePaths(query, pathString, constraint.getShapeResource().getModel());
			}
		}
		catch(QueryParseException ex) {
			throw new SHACLException("Invalid SPARQL constraint (" + ex.getLocalizedMessage() + "):\n" + queryString);
		}

		if(!query.isSelectType()) {
			throw new IllegalArgumentException("SHACL constraints must be SELECT queries");
		}
	}

	
	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		
		QuerySolutionMap bindings = new QuerySolutionMap();
		addBindings(constraint, bindings);
		bindings.add(SH.currentShapeVar.getVarName(), constraint.getShapeResource());
		bindings.add(SH.shapesGraphVar.getVarName(), ResourceFactory.createResource(engine.getShapesGraphURI().toString()));
		
		Resource path = constraint.getShapeResource().getPath();
		if(path != null && path.isURIResource()) {
			bindings.add(SH.PATHVar.getName(), path);
		}
		
		URI oldShapesGraphURI = HasShapeFunction.getShapesGraphURI();
		ShapesGraph oldShapesGraph = HasShapeFunction.getShapesGraph();
		if(!engine.getShapesGraphURI().equals(oldShapesGraphURI)) {
			HasShapeFunction.setShapesGraph(engine.getShapesGraph(), engine.getShapesGraphURI());
		}
		
		Model oldNestedResults = HasShapeFunction.getResultsModel();
		Model nestedResults = JenaUtil.createMemoryModel();
		HasShapeFunction.setResultsModel(nestedResults);
		
		try {
			long startTime = System.currentTimeMillis();
			Resource messageHolder = getSPARQLExecutable(constraint);
			for(RDFNode focusNode : focusNodes) {
				bindings.add(SH.thisVar.getVarName(), focusNode); // Overwrite any previous binding
				QueryExecution qexec = SPARQLSubstitutions.createQueryExecution(query, engine.getDataset(), bindings);
				executeSelectQuery(engine, constraint, messageHolder, nestedResults, focusNode, qexec, bindings);
				engine.checkCanceled();
			}			
			if(ExecStatisticsManager.get().isRecording()) {
				long endTime = System.currentTimeMillis();
				long duration = endTime - startTime;
				String label = getLabel(constraint);
				Iterator<String> varNames = bindings.varNames();
				if(varNames.hasNext()) {
					queryString += "\nBindings:";
					while(varNames.hasNext()) {
						String varName = varNames.next();
						queryString += "\n- ?" + varName + ": " + bindings.get(varName);
					}
				}
				ExecStatistics stats = new ExecStatistics(label, queryString, duration, startTime, constraint.getComponent().asNode());
				ExecStatisticsManager.get().add(Collections.singletonList(stats));
			}
		}
		finally {
			HasShapeFunction.setShapesGraph(oldShapesGraph, oldShapesGraphURI);
			HasShapeFunction.setResultsModel(oldNestedResults);
		}
	}

	
	protected abstract void addBindings(Constraint constraint, QuerySolutionMap bindings);
	
	
	protected abstract Resource getSPARQLExecutable(Constraint constraint);
	
	
	protected abstract String getLabel(Constraint constraint);
	
	
	protected Query getQuery() {
		return query;
	}
	
	
	protected abstract String getSPARQL(Constraint constraint);
	

	private void executeSelectQuery(ValidationEngine engine, Constraint constraint, Resource messageHolder, Model nestedResults,
			RDFNode focusNode, QueryExecution qexec, QuerySolution bindings) {
		
		ResultSet rs = qexec.execSelect();
		
		if(!rs.getResultVars().contains("this")) {
			qexec.close();
			throw new IllegalArgumentException("SELECT constraints must return $this");
		}
		
		try {
			if(rs.hasNext()) {
				while(rs.hasNext()) {
					QuerySolution sol = rs.next();
					RDFNode thisValue = sol.get(SH.thisVar.getVarName());
					if(thisValue != null) {
						Resource resultType = SH.ValidationResult;
						RDFNode selectMessage = sol.get(SH.message.getLocalName());
						if(JenaDatatypes.TRUE.equals(sol.get(SH.failureVar.getName()))) {
							resultType = DASH.FailureResult;
							String message = getLabel(constraint);
							message += " has produced ?" + SH.failureVar.getName();
							if(focusNode != null) {
								message += " for focus node ";
								if(focusNode.isLiteral()) {
									message += focusNode;
								}
								else {
									message += RDFLabels.get().getLabel((Resource)focusNode);
								}
							}
							FailureLog.get().logFailure(message);
							selectMessage = ResourceFactory.createTypedLiteral("Validation Failure: Could not validate shape");
						}
						
						Resource result = engine.createResult(resultType, constraint, thisValue);
						if(SH.SPARQLConstraintComponent.equals(constraint.getComponent())) {
							result.addProperty(SH.sourceConstraint, constraint.getParameterValue());
						}
						
						if(selectMessage != null) {
							result.addProperty(SH.resultMessage, selectMessage);
						}
						else if(constraint.getShapeResource().hasProperty(SH.message)) {
							for(Statement s : constraint.getShapeResource().listProperties(SH.message).toList()) {
								result.addProperty(SH.resultMessage, s.getObject());
							}
						}
						else {
							addDefaultMessages(engine, messageHolder, constraint.getComponent(), result, bindings, sol);
						}
						
						RDFNode pathValue = sol.get(SH.pathVar.getVarName());
						if(pathValue != null && pathValue.isURIResource()) {
							result.addProperty(SH.resultPath, pathValue);
						}
						else if(constraint.getShapeResource().isPropertyShape()) {
							Resource basePath = constraint.getShapeResource().getPropertyResourceValue(SH.path);
							result.addProperty(SH.resultPath, SHACLPaths.clonePath(basePath, result.getModel()));
						}
						
						if(!SH.HasValueConstraintComponent.equals(constraint.getComponent())) { // See https://github.com/w3c/data-shapes/issues/111
							RDFNode selectValue = sol.get(SH.valueVar.getVarName());
							if(selectValue != null) {
								result.addProperty(SH.value, selectValue);
							}
							else if(SH.NodeShape.equals(constraint.getContext())) {
								result.addProperty(SH.value, focusNode);
							}
						}
						
						if(engine.getConfiguration().getReportDetails()) {
							addDetails(result, nestedResults);
						}
					}
				}
			}
			else if(createSuccessResults) {
				Resource success = engine.createResult(DASH.SuccessResult, constraint, focusNode);
				if(SH.SPARQLConstraintComponent.equals(constraint.getComponent())) {
					success.addProperty(SH.sourceConstraint, constraint.getParameterValue());
				}
				if(engine.getConfiguration().getReportDetails()) {
					addDetails(success, nestedResults);
				}
			}
		}
		finally {
			qexec.close();
		}
	}

	
	private void addDefaultMessages(ValidationEngine engine, Resource messageHolder, Resource fallback, Resource result, 
				QuerySolution bindings, QuerySolution solution) {
		boolean found = false;
		for(Statement s : messageHolder.listProperties(SH.message).toList()) {
			if(s.getObject().isLiteral()) {
				QuerySolutionMap map = new QuerySolutionMap();
				map.addAll(bindings);
				map.addAll(solution);
				engine.addResultMessage(result, s.getLiteral(), map);
				found = true;
			}
		}
		if(!found && fallback != null) {
			addDefaultMessages(engine, fallback, null, result, bindings, solution);
		}
	}

	
	public static void addDetails(Resource parentResult, Model nestedResults) {
		if(!nestedResults.isEmpty()) {
			parentResult.getModel().add(nestedResults);
			for(Resource type : SHACLUtil.RESULT_TYPES) {
				for(Resource nestedResult : nestedResults.listSubjectsWithProperty(RDF.type, type).toList()) {
					if(!parentResult.getModel().contains(null, SH.detail, nestedResult)) {
						parentResult.addProperty(SH.detail, nestedResult);
					}
				}
			}
		}
	}
}
