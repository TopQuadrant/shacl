package org.topbraid.shacl.constraints.sparql;

import java.util.HashMap;
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
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.arq.functions.ScopeContainsPFunction;
import org.topbraid.shacl.constraints.ModelConstraintValidator;
import org.topbraid.shacl.constraints.SHACLException;
import org.topbraid.shacl.vocabulary.SH;
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
	
	
	static QueryExecution createQueryExecution(Query query, Dataset dataset, QuerySolution bindings) {
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
	static Query insertFilterClause(Query query, int filterCount) {
		String str = query.toString();
		Pattern pattern = Pattern.compile("(?i)WHERE\\s*\\{");
		Matcher matcher = pattern.matcher(str);
		if(matcher.find()) {
			int index = matcher.end();
			StringBuilder sb = new StringBuilder(str);
			
			StringBuffer s = new StringBuffer();
			for(int i = 0; i < filterCount; i++) {
				s.append("{ FILTER <");
				s.append(SH.hasShape.getURI());
				s.append(">(?this, ?");
				s.append(ModelConstraintValidator.FILTER_VAR_NAME + i);
				s.append(", ?" + SH.shapesGraphVar.getVarName() + ") }");
			}
			
			sb.insert(index, s.toString());
			return ARQFactory.get().createQuery(sb.toString());
		}
		else {
			throw new IllegalArgumentException("Cannot find first '{' in query string: " + str);
		}
	}

	
	// TODO: Algorithm incorrect, e.g. if { is included as a comment
	static Query insertScopeAndFilterClauses(Query query, int filterCount, Resource shape, Dataset dataset, QuerySolution binding) {
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
			appendScopes(s, shape, dataset);
			s.append("        }    }\n");
			for(int i = 0; i < filterCount; i++) {
				s.append("    FILTER <");
				s.append(SH.hasShape.getURI());
				s.append(">(?this, ?");
				s.append(ModelConstraintValidator.FILTER_VAR_NAME + i);
				s.append(", ?" + SH.shapesGraphVar.getVarName() + ") .");
			}
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
						if(labelFunction != null) {
							buffer.append(labelFunction.apply(varValue));
						}
						else if(varValue instanceof Resource) {
							buffer.append(SPINLabels.get().getLabel((Resource)varValue));
						}
						else if(varValue instanceof Literal) {
							buffer.append(varValue.asNode().getLiteralLexicalForm());
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
	
	
	private static void appendScopes(StringBuffer sb, Resource shape, Dataset dataset) {
		
		List<String> scopes = new LinkedList<String>();
		
		if(shape.getModel().contains(shape, SH.scopeNode, (RDFNode)null)) {
			scopes.add("        GRAPH $shapesGraph { $" + SH.currentShapeVar.getName() + " <" + SH.scopeNode + "> ?this } .\n");
		}
		
		if(JenaUtil.hasIndirectType(shape, RDFS.Class)) {
			String varName = "?CLASS_VAR";
			scopes.add("        " + varName + " <" + RDFS.subClassOf + ">* $" + SH.currentShapeVar.getName() + " .\n            ?this a " + varName + " .\n");
		}
		
		for(Resource cls : JenaUtil.getResourceProperties(shape, SH.scopeClass)) {
			String varName = "?SHAPE_CLASS_VAR";
			scopes.add("        " + varName + " <" + RDFS.subClassOf + ">* <" + cls + "> .\n            ?this a " + varName + " .\n");
		}

		int index = 0;
		for(Resource property : JenaUtil.getResourceProperties(shape, SH.scopeProperty)) {
			scopes.add("        ?this <" + property + "> ?ANY_VALUE_" + index++ + " .\n");
		}
		for(Resource property : JenaUtil.getResourceProperties(shape, SH.scopeInverseProperty)) {
			scopes.add("        ?ANY_VALUE_" + index++ + "  <" + property + "> ?this .\n");
		}
		
		if(shape.hasProperty(SH.scope)) {
			scopes.add(createScopes(shape));
		}
		
		if(scopes.isEmpty()) {
			throw new SHACLException("Unscoped shape " + shape);
		}
		else if(scopes.size() == 1) {
			sb.append(scopes.get(0));
		}
		else {
			for(int i = 0; i < scopes.size(); i++) {
				sb.append("        {");
				sb.append(scopes.get(i));
				sb.append("        }");
				if(i < scopes.size() - 1) {
					sb.append("        UNION\n");
				}
			}
		}
	}
	
	
	private static String createScopes(Resource shape) {
		String scopeVar = "?scpe_" + (int)(Math.random() * 10000);
		return  "        GRAPH $" + SH.shapesGraphVar.getName() + " { $" + SH.currentShapeVar.getName() + " <" + SH.scope + "> " + scopeVar + "} .\n" +
				"        (" + scopeVar + " $" + SH.shapesGraphVar.getName() + ") <" + ScopeContainsPFunction.URI + "> ?this .\n";
	}
 }
