package org.topbraid.shacl.constraints.sparql;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.arq.functions.HasShapeFunction;
import org.topbraid.shacl.constraints.ComponentConstraintExecutable;
import org.topbraid.shacl.constraints.ConstraintExecutable;
import org.topbraid.shacl.constraints.ExecutionLanguage;
import org.topbraid.shacl.constraints.FailureLog;
import org.topbraid.shacl.constraints.SHACLException;
import org.topbraid.shacl.model.SHConstraint;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHParameterizableTarget;
import org.topbraid.shacl.util.SHACLUtil;
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
	
	// Flag to generate sh:details for all violations.
	public static boolean createDetails = false;
	
	// Flag to bypass sh:prefixes and instead use all prefixes in the Jena object of the shapes graph.
	public static boolean useGraphPrefixes = false;
	
	private static SPARQLExecutionLanguage singleton = new SPARQLExecutionLanguage();
	
	public static SPARQLExecutionLanguage get() {
		return singleton;
	}
 
	
	@Override
	public SHConstraint asConstraint(Resource c) {
		return SHFactory.asSPARQLConstraint(c);
	}


	@Override
	public boolean canExecuteConstraint(ConstraintExecutable executable) {
		if(SHFactory.isSPARQLConstraint(executable.getConstraint()) &&
				executable.getConstraint().hasProperty(SH.select)) {
			return true;
		}
		else if(executable instanceof ComponentConstraintExecutable) {
			ComponentConstraintExecutable cce = (ComponentConstraintExecutable)executable;
			Resource validator = cce.getValidator(SH.SPARQLExecutable);
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
		StringBuffer sb = new StringBuffer("SELECT ?this ?value ?message");
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
		return withPrefixes(sb.toString(), valuesDeriver);
	}


	@Override
	public boolean executeConstraint(Dataset dataset, Resource shape, URI shapesGraphURI,
			ConstraintExecutable executable, RDFNode focusNode, Resource report,
			Function<RDFNode,String> labelFunction, List<Resource> resultsList) {
		
		if(executable.getConstraint().isDeactivated()) {
			return false;
		}

		String queryString = getSPARQL(executable);
		SHConstraint constraint = executable.getConstraint();
		if(queryString == null) {
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
		
		List<RDFNode> focusNodes;
		
		if(focusNode == null) {
			focusNodes = selectFocusNodes(shape, dataset, shapesGraphURI);
			if(focusNodes.isEmpty()) {
				// Bypass everything if set of focus nodes is empty
				return false;
			}
		}
		else {
			focusNodes = Collections.singletonList(focusNode);
		}
		
		if(SHFactory.isPropertyShape(executable.getConstraint()) || SHFactory.isParameter(executable.getConstraint())) {
			String path = SHACLPaths.getPathString(JenaUtil.getResourceProperty(executable.getConstraint(), SH.path));
			query = SPARQLSubstitutions.substitutePaths(query, path, shape.getModel());
			bindings.add(SH.currentShapeVar.getVarName(), executable.getConstraint());
		}
		else if(!(executable instanceof ComponentConstraintExecutable) && SHFactory.isPropertyShape(shape)) {
			String path = SHACLPaths.getPathString(JenaUtil.getResourceProperty(shape, SH.path));
			query = SPARQLSubstitutions.substitutePaths(query, path, shape.getModel());
			bindings.add(SH.currentShapeVar.getVarName(), shape);
		}
		else {
			bindings.add(SH.currentShapeVar.getVarName(), shape);
		}
		bindings.add(SH.shapesGraphVar.getVarName(), ResourceFactory.createResource(shapesGraphURI.toString()));

		long startTime = System.currentTimeMillis();
		
		URI oldShapesGraphURI = HasShapeFunction.getShapesGraph();
		HasShapeFunction.setShapesGraph(shapesGraphURI);
		
		Model oldNestedResults = HasShapeFunction.getResultsModel();
		Model nestedResults = JenaUtil.createMemoryModel();
		HasShapeFunction.setResultsModel(nestedResults);
		
		int violationCount = 0;
		try {
			// Brute force algorithm: execute for each focus node individually
			for(RDFNode fn : focusNodes) {
				bindings.add(SH.thisVar.getVarName(), fn); // Overwrite any previous binding
				QueryExecution qexec = SPARQLSubstitutions.createQueryExecution(query, dataset, bindings);
				violationCount += executeSelectQuery(report, nestedResults, constraint, shape, focusNode, executable, qexec, labelFunction, resultsList);
			}			
		}
		finally {
			HasShapeFunction.setShapesGraph(oldShapesGraphURI);
			HasShapeFunction.setResultsModel(oldNestedResults);
		}

		if(SPINStatisticsManager.get().isRecording()) {
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			String label = executable + " (" + violationCount + " violations)";
			Iterator<String> varNames = bindings.varNames();
			if(varNames.hasNext()) {
				queryString += "\nBindings:";
				while(varNames.hasNext()) {
					String varName = varNames.next();
					queryString += "\n- ?" + varName + ": " + bindings.get(varName);
				}
			}
			SPINStatistics stats = new SPINStatistics(label, queryString, duration, startTime, 
					focusNode != null ? focusNode.asNode() : constraint.asNode());
			SPINStatisticsManager.get().add(Collections.singletonList(stats));
		}
		return violationCount > 0;
	}


	private String getSPARQL(ConstraintExecutable executable) {
		SHConstraint constraint = executable.getConstraint();
		if(SHFactory.isSPARQLConstraint(constraint)) {
			return withPrefixes(JenaUtil.getStringProperty(constraint, SH.select), SHFactory.asSPARQLConstraint(constraint));
		}
		else if(executable instanceof ComponentConstraintExecutable) {
			ComponentConstraintExecutable cce = (ComponentConstraintExecutable) executable;
			Resource validator = cce.getValidator(getExecutableType());
			if(validator != null) {
				if(JenaUtil.hasIndirectType(validator, SH.SPARQLAskValidator)) {
					return createSPARQLFromAskValidator(cce, validator);
				}
				else if(JenaUtil.hasIndirectType(validator, SH.SPARQLSelectValidator)) {
					return withPrefixes(JenaUtil.getStringProperty(validator, SH.select), validator);
				}
			}
			else if (SH.DerivedValuesConstraintComponent.equals(cce.getComponent())) {
				return createDerivedValuesSPARQL((Resource)cce.getParameterValue());
			}
		}
		return null;
	}
	

	private static int executeSelectQuery(Resource report, Model nestedResults, SHConstraint constraint, Resource shape,
			RDFNode focusNode, ConstraintExecutable executable,
			QueryExecution qexec, Function<RDFNode,String> labelFunction, List<Resource> resultsList) {
		
		Model results = report.getModel();
	
		ResultSet rs = qexec.execSelect();
		
		if(!rs.getResultVars().contains("this")) {
			qexec.close();
			throw new IllegalArgumentException("SELECT constraints must return $this");
		}
		
		int violationCount = 0;
		try {
			List<Literal> defaultMessages = executable.getMessages();
			if(rs.hasNext()) {
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
						report.addProperty(SH.result, result);
						result.addProperty(SH.resultSeverity, severity);
						result.addProperty(SH.sourceConstraint, constraint);
						result.addProperty(SH.sourceShape, shape);
						if(executable instanceof ComponentConstraintExecutable) {
							result.addProperty(SH.sourceConstraintComponent, ((ComponentConstraintExecutable)executable).getComponent());
						}
						else {
							result.addProperty(SH.sourceConstraintComponent, SH.SPARQLConstraintComponent);
						}

						if(selectMessage != null) {
							result.addProperty(SH.resultMessage, selectMessage);
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
								result.addProperty(SH.resultMessage, SPARQLSubstitutions.withSubstitutions(defaultMessage, sol, labelFunction));
							}
						}
						
						RDFNode resultFocusNode = thisValue;
						if(SHFactory.isPropertyShape(constraint) || SHFactory.isParameter(constraint)) {
							result.addProperty(SH.resultPath, SHACLPaths.clonePath(SHFactory.asPropertyConstraint(constraint).getPath(), result.getModel()));
						}
						else if(!(executable instanceof ComponentConstraintExecutable) && SHFactory.isPropertyShape(shape)) {
							result.addProperty(SH.resultPath, SHACLPaths.clonePath(JenaUtil.getResourceProperty(shape, SH.path), result.getModel()));
						}
						else {
							RDFNode pathValue = sol.get(SH.pathVar.getVarName());
							if(pathValue != null && pathValue.isURIResource()) {
								result.addProperty(SH.resultPath, pathValue);
							}
							/*
							RDFNode focusNodeValue = sol.get(SH.focusNodeVar.getVarName());
							if(focusNodeValue != null) {
								resultFocusNode = focusNodeValue;
							}*/
						}
						
						RDFNode selectValue = sol.get(SH.valueVar.getVarName());
						if(selectValue != null) {
							result.addProperty(SH.value, selectValue);
						}
						
						result.addProperty(SH.focusNode, resultFocusNode);
						
						if(createDetails) {
							addDetails(result, nestedResults);
						}
			
						resultsList.add(result);
						violationCount++;
					}
				}
			}
			else if(createDetails) {
				Resource success = results.createResource(DASH.SuccessResult);
				success.addProperty(SH.sourceShape, shape);
				success.addProperty(SH.sourceConstraint, executable.getConstraint());
				if(executable instanceof ComponentConstraintExecutable) {
					success.addProperty(SH.sourceConstraintComponent, ((ComponentConstraintExecutable)executable).getComponent());
				}
				else {
					success.addProperty(SH.sourceConstraintComponent, SH.SPARQLConstraintComponent);
				}
				if(focusNode != null) {
					success.addProperty(SH.focusNode, focusNode);
				}
				addDetails(success, nestedResults);
				resultsList.add(success);
			}
		}
		finally {
			qexec.close();
		}
		
		return violationCount;
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

	
	@Override
	public Iterable<RDFNode> executeTarget(Dataset dataset, Resource target, SHParameterizableTarget parameterizableTarget) {

		String sparql = JenaUtil.getStringProperty(target, SH.select);
		String queryString = withPrefixes(sparql, target);
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
	public Resource getConstraintComponent() {
		return SH.SPARQLConstraintComponent;
	}


	@Override
	public boolean isNodeInTarget(RDFNode focusNode, Dataset dataset, Resource executable, SHParameterizableTarget parameterizableTarget) {

		// If sh:sparql exists only, then we expect run the query with ?this pre-bound
		String sparql = JenaUtil.getStringProperty(executable, SH.select);
		String queryString = withPrefixes(sparql, executable);
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
	
	
	@Override
	public Resource getExecutableType() {
		return SH.SPARQLExecutable;
	}


	@Override
	public Property getParameter() {
		return SH.sparql;
	}


	private String createSPARQLFromAskValidator(ComponentConstraintExecutable executable, Resource validator) {
		String valueVar = "?value";
		while(executable.getComponent().getParametersMap().containsKey(valueVar)) {
			valueVar += "_";
		}
		StringBuffer sb = new StringBuffer();
		if(SH.NodeShape.equals(executable.getContext())) {
			sb.append("SELECT $this ?value\nWHERE {\n");
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
			otherVarNames.remove(SH.valueVar.getVarName());
			for(String varName : otherVarNames) {
				sb.append(" ?" + varName);
			}
			
			// Create body
			sb.append("SELECT DISTINCT $this ?value\nWHERE {\n");
			sb.append("    $this $" + SH.PATHVar.getVarName() + " " + valueVar + " .\n");
		}

		String sparql = JenaUtil.getStringProperty(validator, SH.ask);
		int firstIndex = sparql.indexOf('{');
		int lastIndex = sparql.lastIndexOf('}');
		String body = "{" + sparql.substring(firstIndex + 1, lastIndex + 1);
		sb.append("    FILTER NOT EXISTS " + body + "\n}");
		
		return withPrefixes(sb.toString(), validator);
	}
	
	
	private static Query getSPARQLWithSelect(Resource host) {
		String sparql = JenaUtil.getStringProperty(host, SH.select);
		if(sparql == null) {
			throw new SHACLException("Missing sh:sparql at " + host);
		}
		try {
			return ARQFactory.get().createQuery(withPrefixes(sparql, host));
		}
		catch(Exception ex) {
			return ARQFactory.get().createQuery(withPrefixes("SELECT ?this WHERE {" + sparql + "}", host));
		}
	}
	
	
	private List<RDFNode> selectFocusNodes(Resource shape, Dataset dataset, URI shapesGraphURI) {
		List<RDFNode> results = new LinkedList<RDFNode>();
		StringBuffer sb = new StringBuffer("SELECT DISTINCT ?this\nWHERE {\n");
		SPARQLSubstitutions.appendTargets(sb, shape, dataset);
		sb.append("\n}");
		Query query = ARQFactory.get().createQuery(dataset.getDefaultModel(), sb.toString());
		QuerySolutionMap bindings = new QuerySolutionMap();
		bindings.add(SH.currentShapeVar.getName(), shape);
		bindings.add(SH.shapesGraphVar.getVarName(), ResourceFactory.createResource(shapesGraphURI.toString()));
		try(QueryExecution qexec = ARQFactory.get().createQueryExecution(query, dataset, bindings)) {
			ResultSet rs = qexec.execSelect();
			while(rs.hasNext()) {
				RDFNode focusNode = rs.next().get("this");
				if(focusNode != null) {
					results.add(focusNode);
				}
			}
		}
		return results;
	}
	
	
	/**
	 * Gets a parsable SPARQL string based on a fragment and prefix declarations.
	 * Depending on the setting of the flag useGraphPrefixes, this either uses the
	 * prefixes from the Jena graph of the given executable, or strictly uses sh:prefixes.
	 * @param str  the query fragment (e.g. starting with SELECT)
	 * @param executable  the sh:SPARQLExecutable potentially holding the sh:prefixes
	 * @return the parsable SPARQL string
	 */
	public static String withPrefixes(String str, Resource executable) {
		if(useGraphPrefixes) {
			return ARQFactory.get().createPrefixDeclarations(executable.getModel()) + str;
		}
		else {
			StringBuffer sb = new StringBuffer();
			PrefixMapping pm = new PrefixMappingImpl();
			Set<Resource> reached = new HashSet<Resource>();
			for(Resource ontology : JenaUtil.getResourceProperties(executable, SH.prefixes)) {
				String duplicate = collectPrefixes(ontology, pm, reached);
				if(duplicate != null) {
					throw new SHACLException("Duplicate prefix declaration for prefix " + duplicate);
				}
			}
			for(String prefix : pm.getNsPrefixMap().keySet()) {
				sb.append("PREFIX ");
				sb.append(prefix);
				sb.append(": <");
				sb.append(pm.getNsPrefixURI(prefix));
				sb.append(">\n");
			}
			sb.append(str);
			return sb.toString();
		}
	}
	
	
	// Returns the duplicate prefix, if any
	private static String collectPrefixes(Resource ontology, PrefixMapping pm, Set<Resource> reached) {
		
		reached.add(ontology);
		
		for(Resource decl : JenaUtil.getResourceProperties(ontology, SH.declare)) {
			String prefix = JenaUtil.getStringProperty(decl, SH.prefix);
			String ns = JenaUtil.getStringProperty(decl, SH.namespace);
			if(prefix != null && ns != null) {
				String oldNS = pm.getNsPrefixURI(prefix);
				if(oldNS != null && !oldNS.equals(ns)) {
					return prefix;
				}
				pm.setNsPrefix(prefix, ns);
			}
		}
		
		for(Resource imp : JenaUtil.getResourceProperties(ontology, OWL.imports)) {
			if(!reached.contains(imp)) {
				String duplicate = collectPrefixes(imp, pm, reached);
				if(duplicate != null) {
					return duplicate;
				}
			}
		}
		
		return null;
	}
}
