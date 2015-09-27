package org.topbraid.shacl.constraints.sparql;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topbraid.shacl.constraints.ConstraintExecutable;
import org.topbraid.shacl.constraints.ExecutionLanguage;
import org.topbraid.shacl.constraints.FailureLog;
import org.topbraid.shacl.constraints.ModelConstraintValidator;
import org.topbraid.shacl.constraints.SHACLException;
import org.topbraid.shacl.constraints.TemplateConstraintExecutable;
import org.topbraid.shacl.model.SHACLArgument;
import org.topbraid.shacl.model.SHACLConstraint;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLFunction;
import org.topbraid.shacl.model.SHACLShape;
import org.topbraid.shacl.model.SHACLTemplateCall;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.statistics.SPINStatistics;
import org.topbraid.spin.statistics.SPINStatisticsManager;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

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
		if(executable.getResource().hasProperty(SH.sparql)) {
			return true;
		}
		else if(executable instanceof TemplateConstraintExecutable) {
			Resource function = ((TemplateConstraintExecutable)executable).getValidationFunction();
			if(function != null && function.hasProperty(SH.sparql)) {
				return true;
			}
			if(executable.getResource().equals(SH.AbstractDerivedPropertyConstraint) ||
					executable.getResource().equals(SH.AbstractDerivedInversePropertyConstraint)) {
				Resource derivedValues = executable.getTemplateCall().getPropertyResourceValue(SH.derivedValues);
				return derivedValues != null && derivedValues.hasProperty(SH.sparql);
			}
		}
		return false;
	}

	
	@Override
	public boolean canExecuteScope(Resource executable) {
		return executable.hasProperty(SH.sparql);
	}


	@Override
	public void executeConstraint(Dataset dataset, Resource shape, URI shapesGraphURI,
			SHACLConstraint constraint, ConstraintExecutable executable,
			RDFNode focusNode, Model results) {
		
		Resource resource = executable.getResource();
		String sparql = JenaUtil.getStringProperty(resource, SH.sparql);
		if(sparql == null && executable instanceof TemplateConstraintExecutable) {
			sparql = createSPARQLFromValidationFunctionOrDerivedValues((TemplateConstraintExecutable)executable);
		}
		if(sparql == null) {
			String message = "Missing " + SH.PREFIX + ":" + SH.sparql.getLocalName() + " of " + SPINLabels.get().getLabel(resource);
			if(resource.isAnon()) {
				StmtIterator it = resource.getModel().listStatements(null, null, resource);
				if(it.hasNext()) {
					Statement s = it.next();
					it.close();
					message += " at " + SPINLabels.get().getLabel(s.getSubject());
					message += " via " + SPINLabels.get().getLabel(s.getPredicate());
				}
			}
			throw new SHACLException(message);
		}

		String queryString = ARQFactory.get().createPrefixDeclarations(resource.getModel()) + sparql;
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
		SHACLTemplateCall templateCall = executable.getTemplateCall();
		if(templateCall != null) {
			templateCall.addBindings(bindings);
		}
		List<SHACLShape> filters = executable.getFilterShapes();
		for(Resource filter : JenaUtil.getResourceProperties(shape, SH.filterShape)) {
			filters.add(SHACLFactory.asShape(filter));
		}
		if(focusNode == null) {
			if(shape.isURIResource()) {
				query = SPARQLSubstitutions.insertScopeAndFilterClauses(query, filters.size(), shape, dataset, bindings);
			}
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
		
		QueryExecution qexec = ARQFactory.get().createQueryExecution(query, dataset, bindings);

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
					focusNode != null ? focusNode.asNode() : resource.asNode());
			SPINStatisticsManager.get().add(Collections.singletonList(stats));
		}
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
					String message = "Constraint " + SPINLabels.get().getLabel(executable.getResource());
					if(executable.getTemplateCall() != null) {
						message += " of type " + SPINLabels.get().getLabel(executable.getTemplateCall().getTemplate());
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
				if(executable instanceof TemplateConstraintExecutable) {
					result.addProperty(SH.sourceTemplate, ((TemplateConstraintExecutable)executable).getResource());
				}
				
				if(selectMessage != null) {
					result.addProperty(SH.message, selectMessage);
				}
				else {
					for(Literal defaultMessage : defaultMessages) {
						if(executable.getTemplateCall() != null) {
							QuerySolutionMap map = new QuerySolutionMap();
							Iterator<String> varNames = sol.varNames();
							while(varNames.hasNext()) {
								String varName = varNames.next();
								RDFNode value = sol.get(varName);
								if(value != null) {
									map.add(varName, value);
								}
							}
							Map<String,RDFNode> args = ((TemplateConstraintExecutable)executable).getTemplateCall().getArgumentsMapByVarNames();
							for(String varName : args.keySet()) {
								if(!map.contains(varName)) {
									map.add(varName, args.get(varName));
								}
							}
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
	public Iterable<RDFNode> executeScope(Dataset dataset, Resource executable, SHACLTemplateCall templateCall) {

		String sparql = JenaUtil.getStringProperty(executable, SH.sparql);
		String queryString = ARQFactory.get().createPrefixDeclarations(executable.getModel()) + sparql;
		Query query;
		try {
			query = getSPARQLWithSelect(executable);
		}
		catch(QueryParseException ex) {
			throw new SHACLException("Invalid SPARQL scope (" + ex.getLocalizedMessage() + "):\n" + queryString);
		}
		
		QueryExecution qexec = ARQFactory.get().createQueryExecution(query, dataset);

		if(templateCall != null) {
			QuerySolutionMap bindings = new QuerySolutionMap();
			templateCall.addBindings(bindings);
			qexec.setInitialBinding(bindings);
		}

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
		qexec.close();
		return results;
	}

	
	@Override
	public boolean isNodeInScope(RDFNode focusNode, Dataset dataset, Resource executable, SHACLTemplateCall templateCall) {

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
		if(templateCall != null) {
			templateCall.addBindings(bindings);
		}
		QueryExecution qexec = ARQFactory.get().createQueryExecution(query, dataset, bindings);
		ResultSet rs = qexec.execSelect();
		boolean hasNext = rs.hasNext();
		qexec.close();
		return hasNext;

		/* Alternative: a stupid brute-force algorithm
		Iterator<Resource> it = getResourcesInScope(dataset, executable, templateCall).iterator();
		while(it.hasNext()) {
			if(focusNode.equals(it.next())) {
				return true;
			}
		}
		return false;*/
	}
	
	
	private String createSPARQLFromValidationFunctionOrDerivedValues(TemplateConstraintExecutable executable) {
		Resource function = executable.getValidationFunction();
		StringBuffer sb = new StringBuffer("SELECT ?this ");
		if(executable.getResource().equals(SH.AbstractDerivedPropertyConstraint)) {

			sb.append("($this AS ?subject) $predicate (?value AS ?object) ?message");

			String sparql = executable.getTemplateCall().getPropertyResourceValue(SH.derivedValues).getProperty(SH.sparql).getLiteral().getLexicalForm();
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
		else if(executable.getTemplateCall().hasProperty(RDF.type, SH.AbstractDerivedInversePropertyConstraint)) {
			// TODO
		}
		else if(JenaUtil.hasIndirectType(executable.getResource(), SH.NodeConstraintTemplate)) {
			sb.append("\nWHERE {\n");
			sb.append("    FILTER (!<" + function + ">(?this");
			SHACLFunction f = SHACLFactory.asFunction(function);
			Iterator<SHACLArgument> args = f.getOrderedArguments().iterator();
			args.next(); // Skip ?value
			while(args.hasNext()) {
				sb.append(", ?");
				sb.append(args.next().getVarName());
			}
			sb.append(")) .\n}");
		}
		else {
			boolean pvc = JenaUtil.hasIndirectType(executable.getResource(), SH.PropertyValueConstraintTemplate);
			if(pvc) {
				sb.append("(?this AS ?subject) ?predicate (?value AS ?object)");
			}
			else {
				sb.append("(?value AS ?subject) ?predicate (?this AS ?object)");
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
			if(pvc) {
				sb.append("    ?this ?predicate ?value .\n");
			}
			else {
				sb.append("    ?value ?predicate ?this .\n");
			}
			
			sb.append("    FILTER (!<" + function + ">(?value");
			SHACLFunction f = SHACLFactory.asFunction(function);
			Iterator<SHACLArgument> args = f.getOrderedArguments().iterator();
			args.next(); // Skip ?value
			while(args.hasNext()) {
				sb.append(", ?");
				sb.append(args.next().getVarName());
			}
			sb.append(")) .\n}");
			
			/* TODO: Faster variation: insert the body of the ASK query directly.
			 * Was causing unknown issues with Jena when executed for a given resource, requires investigation
			sb.append("    FILTER NOT EXISTS {");
			String sparql = JenaUtil.getStringProperty(function, SH.sparql);
			int startIndex = sparql.indexOf('{');
			int endIndex = sparql.lastIndexOf('}');
			String body = sparql.substring(startIndex + 1, endIndex);
			sb.append(body);
			sb.append("\n    }\n}");
			 */
		}
		
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
