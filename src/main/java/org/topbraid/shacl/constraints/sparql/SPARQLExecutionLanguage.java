package org.topbraid.shacl.constraints.sparql;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.topbraid.shacl.constraints.ComponentConstraintExecutable;
import org.topbraid.shacl.constraints.ConstraintExecutable;
import org.topbraid.shacl.constraints.ExecutionLanguage;
import org.topbraid.shacl.constraints.FailureLog;
import org.topbraid.shacl.constraints.ModelConstraintValidator;
import org.topbraid.shacl.constraints.SHACLException;
import org.topbraid.shacl.model.SHACLConstraint;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLParameterizableScope;
import org.topbraid.shacl.model.SHACLShape;
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
		if(JenaUtil.hasIndirectType(executable.getConstraint(), SH.SPARQLConstraint) &&
				executable.getConstraint().hasProperty(SH.sparql)) {
			return true;
		}
		else if(executable instanceof ComponentConstraintExecutable) {
			ComponentConstraintExecutable cce = (ComponentConstraintExecutable)executable;
			Resource validator = cce.getValidator();
			if(validator != null && validator.hasProperty(SH.sparql)) {
				return true;
			}
			if(SH.DerivedValuesConstraintComponent.equals(cce.getComponent())) {
				Resource valuesDeriver = (Resource)cce.getParameterValue();
				return  valuesDeriver != null && 
						valuesDeriver.hasProperty(RDF.type, SH.SPARQLValuesDeriver) &&
						valuesDeriver.hasProperty(SH.sparql);
			}
		}
		return false;
	}

	
	@Override
	public boolean canExecuteScope(Resource scope) {
		return scope.hasProperty(SH.sparql);
	}
	
	
	private String createDerivedValuesSPARQL(Resource valuesDeriver, boolean inverse) {
		String sparql = JenaUtil.getStringProperty(valuesDeriver, SH.sparql);
		StringBuffer sb = new StringBuffer("SELECT ?this ");
		if(inverse) {
			sb.append("($this AS ?object) $predicate (?value AS ?subject) ?message");
			sb.append("\nWHERE {\n");
			sb.append("    {\n");
			sb.append("        ?value $predicate $this .\n");
			sb.append("        FILTER NOT EXISTS {\n");
			sb.append(sparql);
			sb.append("        }\n");
			sb.append("        BIND (\"Existing value is not among inverse derived values\" AS ?message) .\n");
			sb.append("    }\n");
			sb.append("    UNION {\n");
			sb.append(sparql);
			sb.append("\n");
			sb.append("        FILTER NOT EXISTS {\n");
			sb.append("            ?value $predicate $this .\n");
			sb.append("        }\n");
			sb.append("        BIND (\"Derived value is not among existing inverse values\" AS ?message) .\n");
			sb.append("    }\n");
			sb.append("}");
		}
		else {
			sb.append("($this AS ?subject) $predicate (?value AS ?object) ?message");
			sb.append("\nWHERE {\n");
			sb.append("    {\n");
			sb.append("        $this $predicate ?value .\n");
			sb.append("        FILTER NOT EXISTS {\n");
			sb.append(sparql);
			sb.append("        }\n");
			sb.append("        BIND (\"Existing value is not among derived values\" AS ?message) .\n");
			sb.append("    }\n");
			sb.append("    UNION {\n");
			sb.append(sparql);
			sb.append("\n");
			sb.append("        FILTER NOT EXISTS {\n");
			sb.append("            $this $predicate ?value .\n");
			sb.append("        }\n");
			sb.append("        BIND (\"Derived value is not among existing values\" AS ?message) .\n");
			sb.append("    }\n");
			sb.append("}");
		}
		return sb.toString();
	}


	@Override
	public void executeConstraint(Dataset dataset, Resource shape, URI shapesGraphURI,
			ConstraintExecutable executable, RDFNode focusNode, Model results) {
		
		String sparql = getSPARQL(executable);
		SHACLConstraint constraint = executable.getConstraint();
		if(sparql == null) {
			String message = "Missing " + SH.PREFIX + ":" + SH.sparql.getLocalName() + " of " + SPINLabels.get().getLabel(constraint);
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
		List<SHACLShape> filters = executable.getFilterShapes();
		for(Resource filter : JenaUtil.getResourceProperties(shape, SH.filterShape)) {
			filters.add(SHACLFactory.asShape(filter));
		}
		if(focusNode == null) {
			query = SPARQLSubstitutions.insertScopeAndFilterClauses(query, filters.size(), shape, dataset, bindings);
		}
		else if(!filters.isEmpty()) {
			query = SPARQLSubstitutions.insertFilterClause(query, filters.size());
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
		int violationCount = executeSelectQuery(results, constraint, shape, focusNode, executable, qexec);
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
		SHACLConstraint constraint = executable.getConstraint();
		if(JenaUtil.hasIndirectType(constraint, SH.SPARQLConstraint)) {
			return JenaUtil.getStringProperty(constraint, SH.sparql);
		}
		else if(executable instanceof ComponentConstraintExecutable) {
			ComponentConstraintExecutable cce = (ComponentConstraintExecutable) executable;
			Resource validator = cce.getValidator();
			if(validator != null) {
				if(JenaUtil.hasIndirectType(validator, SH.SPARQLAskValidator)) {
					return createSPARQLFromAskValidator(cce, validator);
				}
				else if(JenaUtil.hasIndirectType(validator, SH.SPARQLSelectValidator)) {
					return JenaUtil.getStringProperty(validator, SH.sparql);
				}
			}
			else if (SH.DerivedValuesConstraintComponent.equals(cce.getComponent())) {
				return createDerivedValuesSPARQL((Resource)cce.getParameterValue(), SHACLFactory.isInversePropertyConstraint(constraint));
			}
		}
		return null;
	}
	

	private static int executeSelectQuery(Model results, SHACLConstraint constraint, Resource shape,
			RDFNode focusNode, ConstraintExecutable executable,
			QueryExecution qexec) {
	
		ResultSet rs = qexec.execSelect();
		int violationCount = 0;
		try {
			List<Literal> defaultMessages = executable.getMessages();
			while(rs.hasNext()) {
				QuerySolution sol = rs.next();
				
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
						result.addProperty(SH.message, SPARQLSubstitutions.withSubstitutions(defaultMessage, sol));
					}
				}
				
				RDFNode selectPath = sol.get(SH.predicateVar.getVarName());
				if(selectPath instanceof Resource) {
					result.addProperty(SH.predicate, selectPath);
				}
				
				RDFNode selectObject = sol.get(SH.objectVar.getVarName());
				if(selectObject != null) {
					result.addProperty(SH.object, selectObject);
				}
				
				RDFNode selectSubject = sol.get(SH.subjectVar.getVarName());
				if(selectSubject instanceof Resource) {
					result.addProperty(SH.subject, selectSubject);
				}
				
				RDFNode thisValue = sol.get(SH.thisVar.getVarName());
				if(thisValue != null) {
					result.addProperty(SH.focusNode, thisValue);
				}
		
				violationCount++;
			}
		}
		finally {
			qexec.close();
		}
		
		return violationCount;
	}

	
	@Override
	public Iterable<RDFNode> executeScope(Dataset dataset, Resource scope, SHACLParameterizableScope parameterizableScope) {

		String sparql = JenaUtil.getStringProperty(scope, SH.sparql);
		String queryString = ARQFactory.get().createPrefixDeclarations(scope.getModel()) + sparql;
		Query query;
		try {
			query = getSPARQLWithSelect(scope);
		}
		catch(QueryParseException ex) {
			throw new SHACLException("Invalid SPARQL scope (" + ex.getLocalizedMessage() + "):\n" + queryString);
		}
		
		QuerySolutionMap bindings = null;
		if(parameterizableScope != null) {
			bindings = new QuerySolutionMap();
			parameterizableScope.addBindings(bindings);
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
	public boolean isNodeInScope(RDFNode focusNode, Dataset dataset, Resource executable, SHACLParameterizableScope parameterizableScope) {

		// If sh:sparql exists only, then we expect run the query with ?this pre-bound
		String sparql = JenaUtil.getStringProperty(executable, SH.sparql);
		String queryString = ARQFactory.get().createPrefixDeclarations(executable.getModel()) + sparql;
		Query query;
		try {
			query = ARQFactory.get().createQuery(queryString);
		}
		catch(QueryParseException ex) {
			throw new SHACLException("Invalid SPARQL scope (" + ex.getLocalizedMessage() + "):\n" + queryString);
		}

		QuerySolutionMap bindings = new QuerySolutionMap();
		bindings.add(SH.thisVar.getVarName(), focusNode);
		if(parameterizableScope != null) {
			parameterizableScope.addBindings(bindings);
		}
		try(QueryExecution qexec = SPARQLSubstitutions.createQueryExecution(query, dataset, bindings)) {
		    ResultSet rs = qexec.execSelect();
		    boolean hasNext = rs.hasNext();
		    return hasNext;
		}

		/* Alternative: a stupid brute-force algorithm
		Iterator<Resource> it = getResourcesInScope(dataset, executable, templateCall).iterator();
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
		StringBuffer sb = new StringBuffer("SELECT ?this ");
		if(SHACLFactory.isNodeConstraint(executable.getConstraint())) {
			sb.append("\nWHERE {\n");
			sb.append("    BIND ($this AS ");
			sb.append(valueVar);
			sb.append(") .\n");
		}
		else {
			boolean inverse = SHACLFactory.isInversePropertyConstraint(executable.getConstraint());
			if(inverse) {
				sb.append("(" + valueVar + " AS ?subject) $predicate ($this AS ?object)");
			}
			else {
				sb.append("($this AS ?subject) $predicate (" + valueVar + " AS ?object)");
			}
			
			// Collect other variables used in sh:messages
			Set<String> otherVarNames = new HashSet<String>();
			for(Literal message : executable.getMessages()) {
				SPARQLSubstitutions.addMessageVarNames(message.getLexicalForm(), otherVarNames);
			}
			otherVarNames.remove("subject");
			otherVarNames.remove("predicate");
			otherVarNames.remove("object");
			for(String varName : otherVarNames) {
				sb.append(" ?" + varName);
			}
			
			// Create body
			sb.append("\nWHERE {\n");
			if(inverse) {
				sb.append("    " + valueVar + " $predicate $this .\n");
			}
			else {
				sb.append("    $this $predicate " + valueVar + " .\n");
			}
		}

		String sparql = JenaUtil.getStringProperty(validator, SH.sparql);
		int firstIndex = sparql.indexOf('{');
		int lastIndex = sparql.lastIndexOf('}');
		// Temp hack injecting a dummy BIND to work-around ISSUE in jena if optFilterPlacement is on
		String body = "{ BIND(true AS ?qyueyru). " + sparql.substring(firstIndex + 1, lastIndex + 1);
		sb.append("    FILTER NOT EXISTS " + body + "\n}");
		
		return sb.toString();
	}
	
	
	private static Query getSPARQLWithSelect(Resource host) {
		String sparql = JenaUtil.getStringProperty(host, SH.sparql);
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
