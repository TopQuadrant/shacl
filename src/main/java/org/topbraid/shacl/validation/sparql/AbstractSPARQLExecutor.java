package org.topbraid.shacl.validation.sparql;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.arq.functions.HasShapeFunction;
import org.topbraid.shacl.util.FailureLog;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.validation.Constraint;
import org.topbraid.shacl.validation.ConstraintExecutor;
import org.topbraid.shacl.validation.SHACLException;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.statistics.SPINStatistics;
import org.topbraid.spin.statistics.SPINStatisticsManager;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;

public abstract class AbstractSPARQLExecutor implements ConstraintExecutor {
	
	// Flag to generate sh:details for all violations.
	public static boolean createDetails = false;
	
	private Query query;
	
	private String queryString;
	
	
	protected AbstractSPARQLExecutor(Constraint constraint) {
		this.queryString = getSPARQL(constraint);
		try {
			this.query = ARQFactory.get().createQuery(queryString);
		}
		catch(QueryParseException ex) {
			throw new SHACLException("Invalid SPARQL constraint (" + ex.getLocalizedMessage() + "):\n" + queryString);
		}

		if(!query.isSelectType()) {
			throw new IllegalArgumentException("SHACL constraints must be SELECT queries");
		}
	}

	
	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, List<RDFNode> focusNodes) {
		
		QuerySolutionMap bindings = new QuerySolutionMap();
		addBindings(constraint, bindings);
		bindings.add(SH.currentShapeVar.getVarName(), constraint.getShapeResource());
		bindings.add(SH.shapesGraphVar.getVarName(), ResourceFactory.createResource(engine.getShapesGraphURI().toString()));
		
		Resource path = constraint.getShapeResource().getPath();
		if(path != null) {
			if(path.isAnon()) {
				String pathString = SHACLPaths.getPathString(JenaUtil.getResourceProperty(constraint.getShapeResource(), SH.path));
				query = SPARQLSubstitutions.substitutePaths(query, pathString, constraint.getShapeResource().getModel());
			}
			else {
				bindings.add(SH.PATHVar.getName(), path);
			}
		}
		
		URI oldShapesGraphURI = HasShapeFunction.getShapesGraph();
		HasShapeFunction.setShapesGraph(engine.getShapesGraphURI());
		
		Model oldNestedResults = HasShapeFunction.getResultsModel();
		Model nestedResults = JenaUtil.createMemoryModel();
		HasShapeFunction.setResultsModel(nestedResults);
		
		try {
			long startTime = System.currentTimeMillis();
			for(RDFNode focusNode : focusNodes) {
				bindings.add(SH.thisVar.getVarName(), focusNode); // Overwrite any previous binding
				QueryExecution qexec = SPARQLSubstitutions.createQueryExecution(query, engine.getDataset(), bindings);
				executeSelectQuery(engine, constraint, nestedResults, focusNode, qexec, bindings);
			}			
			if(SPINStatisticsManager.get().isRecording()) {
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
				SPINStatistics stats = new SPINStatistics(label, queryString, duration, startTime, constraint.getComponent().asNode());
				SPINStatisticsManager.get().add(Collections.singletonList(stats));
			}
		}
		finally {
			HasShapeFunction.setShapesGraph(oldShapesGraphURI);
			HasShapeFunction.setResultsModel(oldNestedResults);
		}
	}

	
	protected abstract void addBindings(Constraint constraint, QuerySolutionMap bindings);
	
	
	protected abstract Resource getSPARQLExecutable(Constraint constraint);
	
	
	protected abstract String getLabel(Constraint constraint);
	
	
	protected abstract String getSPARQL(Constraint constraint);
	

	private void executeSelectQuery(ValidationEngine engine, Constraint constraint, Model nestedResults,
			RDFNode focusNode, QueryExecution qexec, QuerySolution bindings) {
		
		ResultSet rs = qexec.execSelect();
		
		if(!rs.getResultVars().contains("this")) {
			qexec.close();
			throw new IllegalArgumentException("SELECT constraints must return $this");
		}
		
		Resource messageHolder = getSPARQLExecutable(constraint);
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
									message += SPINLabels.get().getLabel((Resource)focusNode);
								}
							}
							FailureLog.get().logFailure(message);
							selectMessage = ResourceFactory.createTypedLiteral("Validation Failure: Could not validate shape");
						}
						
						Resource result = engine.createResult(resultType, constraint, focusNode);
						if(SH.SPARQLConstraintComponent.equals(constraint.getComponent())) {
							result.addProperty(SH.sourceConstraint, constraint.getParameterValue());
						}
						
						if(selectMessage != null) {
							result.addProperty(SH.resultMessage, selectMessage);
						}
						else {
							addDefaultMessages(engine, messageHolder, constraint.getComponent(), result, bindings, sol);
						}
						
						RDFNode resultFocusNode = thisValue;
						RDFNode pathValue = sol.get(SH.pathVar.getVarName());
						if(pathValue != null && pathValue.isURIResource()) {
							result.addProperty(SH.resultPath, pathValue);
						}
						else if(constraint.getShapeResource().isPropertyShape()) {
							Resource basePath = JenaUtil.getResourceProperty(constraint.getShapeResource(), SH.path);
							result.addProperty(SH.resultPath, SHACLPaths.clonePath(basePath, result.getModel()));
						}
						
						RDFNode selectValue = sol.get(SH.valueVar.getVarName());
						if(selectValue != null) {
							result.addProperty(SH.value, selectValue);
						}
						
						result.addProperty(SH.focusNode, resultFocusNode);
						
						if(createDetails) {
							addDetails(result, nestedResults);
						}
					}
				}
			}
			else if(createDetails) {
				Resource success = engine.createResult(DASH.SuccessResult, constraint, focusNode);
				if(SH.SPARQLConstraintComponent.equals(constraint.getComponent())) {
					success.addProperty(SH.sourceConstraint, constraint.getParameterValue());
				}
				addDetails(success, nestedResults);
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
