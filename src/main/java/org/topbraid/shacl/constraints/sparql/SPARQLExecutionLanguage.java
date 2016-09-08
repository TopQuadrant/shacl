package org.topbraid.shacl.constraints.sparql;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.constraints.ComponentConstraintExecutable;
import org.topbraid.shacl.constraints.ConstraintExecutable;
import org.topbraid.shacl.constraints.ExecutionLanguage;
import org.topbraid.shacl.constraints.FailureLog;
import org.topbraid.shacl.constraints.ModelConstraintValidator;
import org.topbraid.shacl.constraints.SHACLException;
import org.topbraid.shacl.model.SHConstraint;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHParameterizableTarget;
import org.topbraid.shacl.model.SHShape;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.statistics.SPINStatistics;
import org.topbraid.spin.statistics.SPINStatisticsManager;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;

/**
 * The ExecutionLanguage for SPARQL-based SHACL constraints.
 * 
 * @author Holger Knublauch
 */
public class SPARQLExecutionLanguage implements ExecutionLanguage {
	
	private static SPARQLExecutionLanguage singleton = new SPARQLExecutionLanguage();
	
	public static SPARQLExecutionLanguage get() {
		return singleton;
	}
 
	
	@Override
	public boolean canExecuteConstraint(ConstraintExecutable executable) {
		if(SHFactory.isSPARQLConstraint(executable.getConstraint()) &&
				executable.getConstraint().hasProperty(SH.select)) {
			return true;
		}
		else if(executable instanceof ComponentConstraintExecutable) {
			ComponentConstraintExecutable cce = (ComponentConstraintExecutable)executable;
			Resource validator = cce.getValidator();
			if(validator != null && (validator.hasProperty(SH.select) || validator.hasProperty(SH.ask))) {
				return true;
			}
			if(SH.DerivedValuesConstraintComponent.equals(cce.getComponent())) {
				Resource valuesDeriver = (Resource)cce.getParameterValue();
				return  valuesDeriver != null && 
						valuesDeriver.hasProperty(RDF.type, SH.SPARQLValuesDeriver) &&
						valuesDeriver.hasProperty(SH.select);
			}
		}
		return false;
	}

	
	@Override
	public boolean canExecuteTarget(Resource target) {
		return target.hasProperty(SH.select);
	}
	
	
	private String createDerivedValuesSPARQL(Resource valuesDeriver) {
		String sparql = JenaUtil.getStringProperty(valuesDeriver, SH.select);
		int startIndex = sparql.indexOf('{');
		int endIndex = sparql.lastIndexOf('}');
		sparql = sparql.substring(startIndex + 1, endIndex - 1);
		StringBuffer sb = new StringBuffer("SELECT ?this ");
		sb.append("?value ?message");
		sb.append("\nWHERE {\n");
		sb.append("    {\n");
		sb.append("        $this $" + SH.PATHVar.getVarName() + " ?value .\n");
		sb.append("        FILTER NOT EXISTS {\n");
		sb.append(sparql);
		sb.append("        }\n");
		sb.append("        BIND (\"Existing value is not among derived values\" AS ?message) .\n");
		sb.append("    }\n");
		sb.append("    UNION {\n");
		sb.append(sparql);
		sb.append("\n");
		sb.append("        FILTER NOT EXISTS {\n");
		sb.append("            $this $" + SH.PATHVar.getVarName() + " ?value .\n");
		sb.append("        }\n");
		sb.append("        BIND (\"Derived value is not among existing values\" AS ?message) .\n");
		sb.append("    }\n");
		sb.append("}");
		return sb.toString();
	}


	@Override
	public void executeConstraint(Dataset dataset, Resource shape, URI shapesGraphURI,
			ConstraintExecutable executable, RDFNode focusNode, Model results, Function<RDFNode,String> labelFunction) {

		List<SHShape> filters = executable.getFilterShapes();
		if(filters.contains(DASH.None)) {
			return;
		}
		
		String sparql = getSPARQL(executable);
		SHConstraint constraint = executable.getConstraint();
		if(sparql == null) {
			String message = "Missing " + SH.PREFIX + ":" + SH.select.getLocalName() + " of " + SPINLabels.get().getLabel(constraint);
			if(constraint.isAnon()) {
				StmtIterator it = constraint.getModel().listStatements(null, null, constraint);
				if(it.hasNext()) {
					Statement s = it.next();
					it.close();
					message += " at " + SPINLabels.get().getLabel(s.getSubject());
					message += " via " + SPINLabels.get().getLabel(s.getPredicate());
				}
			}
			throw new SHACLException(message);
		}

		String queryString = ARQFactory.get().createPrefixDeclarations(constraint.getModel()) + sparql;
		Query query;
		try {
			query = ARQFactory.get().createQuery(queryString);
		}
		catch(QueryParseException ex) {
			throw new SHACLException("Invalid SPARQL constraint (" + ex.getLocalizedMessage() + "):\n" + queryString);
		}

		if(!query.isSelectType()) {
			throw new IllegalArgumentException("SHACL constraints must be SELECT queries");
		}
		
		QuerySolutionMap bindings = new QuerySolutionMap();
		if(executable instanceof ComponentConstraintExecutable) {
			((ComponentConstraintExecutable)executable).addBindings(bindings);
		}
		for(Resource filter : JenaUtil.getResourceProperties(shape, SH.filterShape)) {
			filters.add(SHFactory.asShape(filter));
		}
		if(focusNode == null) {
			query = SPARQLSubstitutions.insertTargetAndFilterClauses(query, filters.size(), shape, dataset, bindings);
		}
		else if(!filters.isEmpty()) {
			query = SPARQLSubstitutions.insertFilterClause(query, filters.size());
		}
		
		if(SHFactory.isPropertyConstraint(executable.getConstraint()) || SHFactory.isParameter(executable.getConstraint())) {
			String path = constraint.hasProperty(SH.predicate) ?
				"<" + SHFactory.asPropertyConstraint(executable.getConstraint()).getPredicate() + ">" :
				SHACLPaths.getPathString(JenaUtil.getResourceProperty(executable.getConstraint(), SH.path));
			query = SPARQLSubstitutions.substitutePaths(query, path, shape.getModel());
		}

		if(focusNode != null) {
			bindings.add(SH.thisVar.getVarName(), focusNode);
		}
		bindings.add(SH.currentShapeVar.getVarName(), shape);
		bindings.add(SH.shapesGraphVar.getVarName(), ResourceFactory.createResource(shapesGraphURI.toString()));
		for(int i = 0; i < filters.size(); i++) {
			bindings.add(ModelConstraintValidator.FILTER_VAR_NAME + i, filters.get(i));
		}
		
		QueryExecution qexec = SPARQLSubstitutions.createQueryExecution(query, dataset, bindings);

		long startTime = System.currentTimeMillis();
		int violationCount = executeSelectQuery(results, constraint, shape, focusNode, executable, qexec, labelFunction);
		if(SPINStatisticsManager.get().isRecording()) {
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			String label = executable + " (" + violationCount + " violations)";
			Iterator<String> varNames = bindings.varNames();
			if(varNames.hasNext()) {
				sparql += "\nBindings:";
				while(varNames.hasNext()) {
					String varName = varNames.next();
					sparql += "\n- ?" + varName + ": " + bindings.get(varName);
				}
			}
			SPINStatistics stats = new SPINStatistics(label, sparql, duration, startTime, 
					focusNode != null ? focusNode.asNode() : constraint.asNode());
			SPINStatisticsManager.get().add(Collections.singletonList(stats));
		}
	}


	private String getSPARQL(ConstraintExecutable executable) {
		SHConstraint constraint = executable.getConstraint();
		if(SHFactory.isSPARQLConstraint(constraint)) {
			return JenaUtil.getStringProperty(constraint, SH.select);
		}
		else if(executable instanceof ComponentConstraintExecutable) {
			ComponentConstraintExecutable cce = (ComponentConstraintExecutable) executable;
			Resource validator = cce.getValidator();
			if(validator != null) {
				if(JenaUtil.hasIndirectType(validator, SH.SPARQLAskValidator)) {
					return createSPARQLFromAskValidator(cce, validator);
				}
				else if(JenaUtil.hasIndirectType(validator, SH.SPARQLSelectValidator)) {
					return JenaUtil.getStringProperty(validator, SH.select);
				}
			}
			else if (SH.DerivedValuesConstraintComponent.equals(cce.getComponent())) {
				return createDerivedValuesSPARQL((Resource)cce.getParameterValue());
			}
		}
		return null;
	}
	

	private static int executeSelectQuery(Model results, SHConstraint constraint, Resource shape,
			RDFNode focusNode, ConstraintExecutable executable,
			QueryExecution qexec, Function<RDFNode,String> labelFunction) {
	
		ResultSet rs = qexec.execSelect();
		int violationCount = 0;
		try {
			List<Literal> defaultMessages = executable.getMessages();
			while(rs.hasNext()) {
				QuerySolution sol = rs.next();
				RDFNode thisValue = sol.get(SH.thisVar.getVarName());
				if(thisValue != null) {
					
					Resource resultType = SH.ValidationResult;
					Resource severity = executable.getSeverity();
					RDFNode selectMessage = sol.get(SH.message.getLocalName());
					if(JenaDatatypes.TRUE.equals(sol.get(SH.failureVar.getName()))) {
						resultType = DASH.FailureResult;
						String message = "Constraint " + SPINLabels.get().getLabel(executable.getConstraint());
						if(executable instanceof ComponentConstraintExecutable) {
							message += " at component " + SPINLabels.get().getLabel(((ComponentConstraintExecutable)executable).getComponent());
						}
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
					
					Resource result = results.createResource(resultType);
					result.addProperty(SH.severity, severity);
					result.addProperty(SH.sourceConstraint, constraint);
					result.addProperty(SH.sourceShape, shape);
					if(executable instanceof ComponentConstraintExecutable) {
						result.addProperty(SH.sourceConstraintComponent, ((ComponentConstraintExecutable)executable).getComponent());
					}
					
					if(selectMessage != null) {
						result.addProperty(SH.message, selectMessage);
					}
					else {
						for(Literal defaultMessage : defaultMessages) {
							if(executable instanceof ComponentConstraintExecutable) {
								QuerySolutionMap map = new QuerySolutionMap();
								Iterator<String> varNames = sol.varNames();
								while(varNames.hasNext()) {
									String varName = varNames.next();
									RDFNode value = sol.get(varName);
									if(value != null) {
										map.add(varName, value);
									}
								}
								((ComponentConstraintExecutable)executable).addBindings(map);
								sol = map;
							}
							result.addProperty(SH.message, SPARQLSubstitutions.withSubstitutions(defaultMessage, sol, labelFunction));
						}
					}
					
					RDFNode resultFocusNode = thisValue;
					if(SHFactory.isPropertyConstraintWithPath(constraint)) {
						result.addProperty(SH.path, SHACLPaths.clonePath(SHFactory.asPropertyConstraint(constraint).getPath(), result.getModel()));
					}
					else if(SHFactory.isPropertyConstraint(constraint) || SHFactory.isParameter(constraint)) {
						result.addProperty(SH.path, SHFactory.asPropertyConstraint(constraint).getPredicate());
					}
					else {
						RDFNode pathValue = sol.get(SH.pathVar.getVarName());
						if(pathValue != null && pathValue.isURIResource()) {
							result.addProperty(SH.path, pathValue);
						}
						RDFNode focusNodeValue = sol.get(SH.focusNodeVar.getVarName());
						if(focusNodeValue != null) {
							resultFocusNode = focusNodeValue;
						}
					}
					
					RDFNode selectValue = sol.get(SH.valueVar.getVarName());
					if(selectValue != null) {
						result.addProperty(SH.value, selectValue);
					}
					
					result.addProperty(SH.focusNode, resultFocusNode);
		
					violationCount++;
				}
			}
		}
		finally {
			qexec.close();
		}
		
		return violationCount;
	}

	
	@Override
	public Iterable<RDFNode> executeTarget(Dataset dataset, Resource target, SHParameterizableTarget parameterizableTarget) {

		String sparql = JenaUtil.getStringProperty(target, SH.select);
		String queryString = ARQFactory.get().createPrefixDeclarations(target.getModel()) + sparql;
		Query query;
		try {
			query = getSPARQLWithSelect(target);
		}
		catch(QueryParseException ex) {
			throw new SHACLException("Invalid SPARQL target (" + ex.getLocalizedMessage() + "):\n" + queryString);
		}
		
		QuerySolutionMap bindings = null;
		if(parameterizableTarget != null) {
			bindings = new QuerySolutionMap();
			parameterizableTarget.addBindings(bindings);
		}
		try(QueryExecution qexec = SPARQLSubstitutions.createQueryExecution(query, dataset, bindings)) {
		    Set<RDFNode> results = new HashSet<RDFNode>();
		    ResultSet rs = qexec.execSelect();
		    List<String> varNames = rs.getResultVars();
		    while(rs.hasNext()) {
		        QuerySolution qs = rs.next();
		        for(String varName : varNames) {
		            RDFNode value = qs.get(varName);
		            if(value != null) {
		                results.add(value);
		            }
		        }
		    }
		    return results;
		}
	}

	
	@Override
	public boolean isNodeInTarget(RDFNode focusNode, Dataset dataset, Resource executable, SHParameterizableTarget parameterizableTarget) {

		// If sh:sparql exists only, then we expect run the query with ?this pre-bound
		String sparql = JenaUtil.getStringProperty(executable, SH.select);
		String queryString = ARQFactory.get().createPrefixDeclarations(executable.getModel()) + sparql;
		Query query;
		try {
			query = ARQFactory.get().createQuery(queryString);
		}
		catch(QueryParseException ex) {
			throw new SHACLException("Invalid SPARQL target (" + ex.getLocalizedMessage() + "):\n" + queryString);
		}

		QuerySolutionMap bindings = new QuerySolutionMap();
		bindings.add(SH.thisVar.getVarName(), focusNode);
		if(parameterizableTarget != null) {
			parameterizableTarget.addBindings(bindings);
		}
		try(QueryExecution qexec = SPARQLSubstitutions.createQueryExecution(query, dataset, bindings)) {
		    ResultSet rs = qexec.execSelect();
		    boolean hasNext = rs.hasNext();
		    return hasNext;
		}

		/* Alternative: a stupid brute-force algorithm
		Iterator<Resource> it = getResourcesInTarget(dataset, executable, templateCall).iterator();
		while(it.hasNext()) {
			if(focusNode.equals(it.next())) {
				return true;
			}
		}
		return false;*/
	}
	
	
	private String createSPARQLFromAskValidator(ComponentConstraintExecutable executable, Resource validator) {
		String valueVar = "?value";
		while(executable.getComponent().getParametersMap().containsKey(valueVar)) {
			valueVar += "_";
		}
		StringBuffer sb = new StringBuffer("SELECT $this ?value");
		if(SH.Shape.equals(executable.getContext())) {
			sb.append("\nWHERE {\n");
			sb.append("    BIND ($this AS ");
			sb.append(valueVar);
			sb.append(") .\n");
		}
		else {
			// Collect other variables used in sh:messages
			Set<String> otherVarNames = new HashSet<String>();
			for(Literal message : executable.getMessages()) {
				SPARQLSubstitutions.addMessageVarNames(message.getLexicalForm(), otherVarNames);
			}
			otherVarNames.remove(SH.pathVar.getVarName());
			otherVarNames.remove(SH.predicateVar.getVarName());
			otherVarNames.remove(SH.valueVar.getVarName());
			for(String varName : otherVarNames) {
				sb.append(" ?" + varName);
			}
			
			// Create body
			sb.append("\nWHERE {\n");
			sb.append("    $this $" + SH.PATHVar.getVarName() + " " + valueVar + " .\n");
		}

		String sparql = JenaUtil.getStringProperty(validator, SH.ask);
		int firstIndex = sparql.indexOf('{');
		int lastIndex = sparql.lastIndexOf('}');
		// Temp hack injecting a dummy BIND to work-around ISSUE in jena if optFilterPlacement is on
		// TODO: The underlying issue seems to have been fixed in Jena 3.1, so this may be deleted.
		String body = "{ BIND(true AS ?qyueyru). " + sparql.substring(firstIndex + 1, lastIndex + 1);
		sb.append("    FILTER NOT EXISTS " + body + "\n}");
		
		return sb.toString();
	}
	
	
	private static Query getSPARQLWithSelect(Resource host) {
		String sparql = JenaUtil.getStringProperty(host, SH.select);
		if(sparql == null) {
			throw new SHACLException("Missing sh:sparql at " + host);
		}
		try {
			return ARQFactory.get().createQuery(host.getModel(), sparql);
		}
		catch(Exception ex) {
			return ARQFactory.get().createQuery(host.getModel(), "SELECT ?this WHERE {" + sparql + "}");
		}
	}
}
