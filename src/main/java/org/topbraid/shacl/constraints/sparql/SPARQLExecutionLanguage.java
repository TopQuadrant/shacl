package org.topbraid.shacl.constraints.sparql;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.topbraid.shacl.constraints.ConstraintExecutable;
import org.topbraid.shacl.constraints.ExecutionLanguage;
import org.topbraid.shacl.constraints.FatalErrorLog;
import org.topbraid.shacl.constraints.ModelConstraintValidator;
import org.topbraid.shacl.constraints.SHACLException;
import org.topbraid.shacl.constraints.TemplateConstraintExecutable;
import org.topbraid.shacl.model.SHACLArgument;
import org.topbraid.shacl.model.SHACLConstraint;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLFunction;
import org.topbraid.shacl.model.SHACLShape;
import org.topbraid.shacl.model.SHACLTemplateCall;
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
			Resource focusNode, Model results) {
		
		Resource resource = executable.getResource();
		String sparql = JenaUtil.getStringProperty(resource, SH.sparql);
		if(sparql == null && executable instanceof TemplateConstraintExecutable) {
			sparql = createSPARQLFromValidationFunction((TemplateConstraintExecutable)executable);
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
			Resource focusNode, ConstraintExecutable executable,
			QueryExecution qexec) {
	
		ResultSet rs = qexec.execSelect();
		int violationCount = 0;
		List<Literal> defaultMessages = executable.getMessages();
		while(rs.hasNext()) {
			QuerySolution sol = rs.next();
			
			Resource severity = executable.getSeverity();
			RDFNode selectMessage = sol.get(SH.messageVar.getVarName());
			if(JenaDatatypes.TRUE.equals(sol.get("error"))) {
				severity = SH.FatalError;
				String message = "Constraint " + SPINLabels.get().getLabel(executable.getResource());
				if(executable.getTemplateCall() != null) {
					message += " of type " + SPINLabels.get().getLabel(executable.getTemplateCall().getTemplate());
				}
				message += " has produced ?error";
				if(focusNode != null) {
					message += " for focus node " + SPINLabels.get().getLabel(focusNode);
				}
				FatalErrorLog.get().log(message);
				selectMessage = ResourceFactory.createTypedLiteral("Fatal Error: Could not validate shape");
			}
			
			Resource vio = results.createResource(severity);
			vio.addProperty(SH.sourceConstraint, constraint);
			vio.addProperty(SH.sourceShape, shape);
			
			if(selectMessage != null) {
				vio.addProperty(SH.message, selectMessage);
			}
			else {
				for(Literal defaultMessage : defaultMessages) {
					vio.addProperty(SH.message, SPARQLSubstitutions.withSubstitutions(defaultMessage, sol));
				}
			}
			
			RDFNode selectPath = sol.get(SH.predicateVar.getVarName());
			if(selectPath instanceof Resource) {
				vio.addProperty(SH.predicate, selectPath);
			}
			else {
				Resource path = executable.getPredicate();
				if(path != null) {
					vio.addProperty(SH.predicate, path);
				}
			}
			
			RDFNode selectObject = sol.get(SH.objectVar.getVarName());
			if(selectObject != null) {
				vio.addProperty(SH.object, selectObject);
			}
			
			RDFNode selectSubject = sol.get(SH.subjectVar.getVarName());
			if(selectSubject instanceof Resource) {
				vio.addProperty(SH.subject, selectSubject);
			}
			
			RDFNode thisValue = sol.get(SH.thisVar.getVarName());
			if(thisValue != null) {
				vio.addProperty(SH.focusNode, thisValue);
			}
	
			violationCount++;
		}
		qexec.close();
		
		return violationCount;
	}

	
	@Override
	public Iterable<Resource> executeScope(Dataset dataset, Resource executable, SHACLTemplateCall templateCall) {

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

		Set<Resource> results = new HashSet<Resource>();
		ResultSet rs = qexec.execSelect();
		List<String> varNames = rs.getResultVars();
		while(rs.hasNext()) {
			QuerySolution qs = rs.next();
			for(String varName : varNames) {
				RDFNode value = qs.get(varName);
				if(value instanceof Resource) {
					results.add((Resource)value);
				}
			}
		}
		qexec.close();
		return results;
	}

	
	@Override
	public boolean isNodeInScope(Resource focusNode, Dataset dataset, Resource executable, SHACLTemplateCall templateCall) {

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
	
	
	private String createSPARQLFromValidationFunction(TemplateConstraintExecutable executable) {
		Resource function = executable.getValidationFunction();
		boolean pvc = JenaUtil.hasIndirectType(executable.getResource(), SH.PropertyValueConstraintTemplate);
		StringBuffer sb = new StringBuffer("SELECT ?this ");
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
