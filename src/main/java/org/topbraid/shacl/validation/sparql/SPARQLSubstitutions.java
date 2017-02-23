package org.topbraid.shacl.validation.sparql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.arq.functions.TargetContainsPFunction;
import org.topbraid.shacl.validation.SHACLException;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.shacl.vocabulary.SHJS;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.util.JenaUtil;

/**
 * Collects various dodgy helper algorithms currently used by the SPARQL execution language.
 * 
 * TODO: These should likely operate on clones of the Query syntax tree instead of query strings.
 *
 * @author Holger Knublauch
 */
public class SPARQLSubstitutions {
	
	// Flag to bypass sh:prefixes and instead use all prefixes in the Jena object of the shapes graph.
	public static boolean useGraphPrefixes = false;

	// Currently switched to old setInitialBinding solution
	private static boolean USE_TRANSFORM = false;
	
	
	public static void addMessageVarNames(String labelTemplate, Set<String> results) {
		for(int i = 0; i < labelTemplate.length(); i++) {
			if(i < labelTemplate.length() - 3 && labelTemplate.charAt(i) == '{' && labelTemplate.charAt(i + 1) == '?') {
				int varEnd = i + 2;
				while(varEnd < labelTemplate.length()) {
					if(labelTemplate.charAt(varEnd) == '}') {
						String varName = labelTemplate.substring(i + 2, varEnd);
						results.add(varName);
						break;
					}
					else {
						varEnd++;
					}
				}
				i = varEnd;
			}
		}
	}
	
	
	public static QueryExecution createQueryExecution(Query query, Dataset dataset, QuerySolution bindings) {
		if(USE_TRANSFORM && bindings != null) {
			Map<Var,Node> substitutions = new HashMap<Var,Node>();
			Iterator<String> varNames = bindings.varNames();
			while(varNames.hasNext()) {
				String varName = varNames.next();
				substitutions.put(Var.alloc(varName), bindings.get(varName).asNode());
			}
			Query newQuery = JenaUtil.queryWithSubstitutions(query, substitutions);
			return ARQFactory.get().createQueryExecution(newQuery, dataset);
		}
		else {
			return ARQFactory.get().createQueryExecution(query, dataset, bindings);
		}
	}

	
	// TODO: Algorithm incorrect, e.g. if { is included as a comment
	static Query insertTargetClauses(Query query, Resource shape, Dataset dataset, QuerySolution binding) {
		String str = query.toString();
		Pattern pattern = Pattern.compile("(?i)WHERE\\s*\\{");
		Matcher matcher = pattern.matcher(str);
		if(matcher.find()) {
			int index = matcher.end();
			StringBuilder sb = new StringBuilder(str);
			
			StringBuffer s = new StringBuffer();
			s.append("    {{\n        SELECT DISTINCT ?" + SH.thisVar.getName() + " ?" + SH.shapesGraphVar.getName() + " ?" + SH.currentShapeVar.getName());

			// We need to enumerate template call arguments here because Jena would otherwise drop the pre-bound variables
			Iterator<String> varNames = binding.varNames();
			while(varNames.hasNext()) {
				String varName = varNames.next();
				s.append(" ?" + varName);
			}
			
			s.append("\nWHERE {\n");
			appendTargets(s, shape, dataset);
			s.append("        }    }\n");
			s.append("}");
			
			sb.insert(index, s.toString());
			try {
				return ARQFactory.get().createQuery(sb.toString());
			}
			catch(QueryParseException ex) {
				System.err.println("Failed to parse query:\n" + sb);
				throw ex;
			}
		}
		else {
			throw new IllegalArgumentException("Cannot find first '{' in query string: " + str);
		}
	}
	
	
	public static Query substitutePaths(Query query, String pathString, Model model) {
		// TODO: This is a bad algorithm - should be operating on syntax tree, not string
		String str = query.toString().replaceAll(" \\?" + SH.PATHVar.getVarName() + " ", pathString);
		return ARQFactory.get().createQuery(model, str);
	}

	
	public static Literal withSubstitutions(Literal template, QuerySolution bindings, Function<RDFNode,String> labelFunction) {
		StringBuffer buffer = new StringBuffer();
		String labelTemplate = template.getLexicalForm();
		for(int i = 0; i < labelTemplate.length(); i++) {
			if(i < labelTemplate.length() - 3 && labelTemplate.charAt(i) == '{' && (labelTemplate.charAt(i + 1) == '?' || labelTemplate.charAt(i + 1) == '$')) {
				int varEnd = i + 2;
				while(varEnd < labelTemplate.length()) {
					if(labelTemplate.charAt(varEnd) == '}') {
						String varName = labelTemplate.substring(i + 2, varEnd);
						RDFNode varValue = bindings.get(varName);
						if(varValue != null) {
							if(labelFunction != null) {
								buffer.append(labelFunction.apply(varValue));
							}
							else if(varValue instanceof Resource) {
								buffer.append(SPINLabels.get().getLabel((Resource)varValue));
							}
							else if(varValue instanceof Literal) {
								buffer.append(varValue.asNode().getLiteralLexicalForm());
							}
						}
						break;
					}
					else {
						varEnd++;
					}
				}
				i = varEnd;
			}
			else {
				buffer.append(labelTemplate.charAt(i));
			}
		}
		if(template.getLanguage().isEmpty()) {
			return ResourceFactory.createTypedLiteral(buffer.toString());
		}
		else {
			return ResourceFactory.createLangLiteral(buffer.toString(), template.getLanguage());
		}
	}
	
	
	static void appendTargets(StringBuffer sb, Resource shape, Dataset dataset) {
		
		List<String> targets = new LinkedList<String>();
		
		if(shape.getModel().contains(shape, SH.targetNode, (RDFNode)null)) {
			targets.add("        GRAPH " + SHJS.SHAPES_VAR + " { $" + SH.currentShapeVar.getName() + " <" + SH.targetNode + "> ?this } .\n");
		}
		
		if(JenaUtil.hasIndirectType(shape, RDFS.Class)) {
			String varName = "?CLASS_VAR";
			targets.add("        " + varName + " <" + RDFS.subClassOf + ">* $" + SH.currentShapeVar.getName() + " .\n            ?this a " + varName + " .\n");
		}
		
		for(Resource cls : JenaUtil.getResourceProperties(shape, SH.targetClass)) {
			String varName = "?SHAPE_CLASS_VAR";
			targets.add("        " + varName + " <" + RDFS.subClassOf + ">* <" + cls + "> .\n            ?this a " + varName + " .\n");
		}

		int index = 0;
		for(Resource property : JenaUtil.getResourceProperties(shape, SH.targetSubjectsOf)) {
			targets.add("        ?this <" + property + "> ?ANY_VALUE_" + index++ + " .\n");
		}
		for(Resource property : JenaUtil.getResourceProperties(shape, SH.targetObjectsOf)) {
			targets.add("        ?ANY_VALUE_" + index++ + "  <" + property + "> ?this .\n");
		}
		
		if(shape.hasProperty(SH.target)) {
			targets.add(createTargets(shape));
		}
		
		if(targets.isEmpty()) {
			throw new SHACLException("Shape without target " + shape);
		}
		else if(targets.size() == 1) {
			sb.append(targets.get(0));
		}
		else {
			for(int i = 0; i < targets.size(); i++) {
				sb.append("        {");
				sb.append(targets.get(i));
				sb.append("        }");
				if(i < targets.size() - 1) {
					sb.append("        UNION\n");
				}
			}
		}
	}
	
	
	private static String createTargets(Resource shape) {
		String targetVar = "?trgt_" + (int)(Math.random() * 10000);
		return  "        GRAPH $" + SH.shapesGraphVar.getName() + " { $" + SH.currentShapeVar.getName() + " <" + SH.target + "> " + targetVar + "} .\n" +
				"        (" + targetVar + " $" + SH.shapesGraphVar.getName() + ") <" + TargetContainsPFunction.URI + "> ?this .\n";
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
