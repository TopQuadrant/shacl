/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.CommandWithWhere;
import org.topbraid.spin.model.Query;
import org.topbraid.spin.model.QueryOrTemplateCall;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Template;
import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.print.Printable;
import org.topbraid.spin.model.print.StringPrintContext;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * Some static util methods for SPIN that don't fit anywhere else.
 * 
 * @author Holger Knublauch
 */
public class SPINUtil {
	
	/**
	 * The name of the variable that will be used in type binding
	 * triple patterns (?this rdf:type ?TYPE_CLASS)
	 */
	public static final String TYPE_CLASS_VAR_NAME = "TYPE_CLASS";


	/**
	 * Collects all queries or template calls at a given class.
	 * @param cls  the class to get the queries at
	 * @param predicate  the predicate such as <code>spin:rule</code>
	 * @param results  the List to add the results to
	 */
	public static void addQueryOrTemplateCalls(Resource cls, Property predicate, List<QueryOrTemplateCall> results) {
		List<Statement> ss = JenaUtil.listAllProperties(cls, predicate).toList();
		
		// Special case: we might have an instance of a template call like spl:Attribute
		//               Then try to find the Template in the registry
		if(ss.isEmpty() && cls != null && cls.isURIResource()) {
			Template template = SPINModuleRegistry.get().getTemplate(cls.getURI(), null);
			if(template != null) {
				ss = JenaUtil.listAllProperties(template, predicate).toList();
			}
		}
		
		for(Statement s : ss) {
			if(s.getObject().isResource()) {
				TemplateCall templateCall = SPINFactory.asTemplateCall(s.getResource());
				if(templateCall != null) {
					results.add(new QueryOrTemplateCall(cls, templateCall));
				}
				else {
					Query query = SPINFactory.asQuery(s.getResource());
					if(query != null) {
						results.add(new QueryOrTemplateCall(cls, query));
					}
				}
			}
		}
	}
	
	
	/**
	 * Inserts a statement  ?this a ?TYPE_CLASS .  after the WHERE { keyword. 
	 * The current implementation is not 100% correct - it is based on a simple
	 * regex matching and somebody could put a WHERE { into a comment to break it.
	 * @param str  the input String
	 */
	public static String addThisTypeClause(String str) {
		
		// TODO: not a correct algorithm, see above
		
		String varName = TYPE_CLASS_VAR_NAME;
		Pattern pattern = Pattern.compile("(?i)WHERE\\s*\\{");
		Matcher matcher = pattern.matcher(str);
		if(matcher.find()) {
			int index = matcher.end();
			StringBuilder sb = new StringBuilder(str);
			sb.insert(index, " ?this a ?" + varName + " . ");
			return sb.toString();
		}
		else {
			throw new IllegalArgumentException("Malformed query: could not find start of WHERE clause");
		}
	}
	

	/**
	 * Applies variable bindings, replacing the values of one map with
	 * the values from a given variables map.
	 * @param map  the Map to modify
	 * @param bindings  the current variable bindings
	 */
	public static void applyBindings(Map<Property,RDFNode> map, Map<String,RDFNode> bindings) {
		for(Property property : new ArrayList<Property>(map.keySet())) {
			RDFNode value = map.get(property);
			Variable var = SPINFactory.asVariable(value);
			if(var != null) {
				String varName = var.getName();
				RDFNode b = bindings.get(varName);
				if(b != null) {
					map.put(property, b);
				}
			}
		}
	}
	

	/**
	 * Binds the variable ?this with a given value.
	 * @param qexec  the QueryExecution to modify
	 * @param value  the value to bind ?this with
	 */
	public static void bindThis(QueryExecution qexec, RDFNode value) {
		if(value != null) {
			QuerySolutionMap bindings = new QuerySolutionMap();
			bindings.add(SPIN.THIS_VAR_NAME, value);
			qexec.setInitialBinding(bindings);
		}
	}
	

	/**
	 * Checks whether a given query mentions the variable ?this anywhere.
	 * This can be used to check whether ?this needs to be bound before
	 * execution, etc.
	 * The current implementation is very primitive in that it only checks
	 * for the string "?this" anywhere in the query, so this method should
	 * only be used if a false positive does not cause problems.
	 * @param command  the query to test
	 * @return true if it was found
	 */
	public static boolean containsThis(CommandWithWhere command) {
		String queryString = ARQFactory.get().createCommandString(command);
		return queryString.contains("?this");
	}
	

	/**
	 * Executes a given SELECT query and returns the first value of the first result
	 * variable, if any exists.  The QueryExecution is closed at the end.
	 * @param qexec  the QueryExecution to execute
	 * @return the first result or null
	 */
	public static RDFNode getFirstResult(QueryExecution qexec) {
		try {
			ResultSet rs = qexec.execSelect();
			if(rs.hasNext()) {
				String varName = rs.getResultVars().get(0);
				RDFNode result = rs.next().get(varName);
				return result;
			}
			else {
				return null;
			}
		} finally {
			qexec.close();
		}
	}
	
	
	/**
	 * Attempts to convert a given RDFNode to a String so that it can be parsed into
	 * a Jena query object.  The node must be either a string Literal, or a sp:Query node
	 * or a template call.  If it's a template call then the resulting query string will
	 * "hard-bind" the template variables.
	 * @param node  the RDFNode to convert
	 * @param usePrefixes  true to use qname abbreviations
	 * @return the String representation of node
	 * @throws IllegalArgumentException  if the node is not a valid SPIN Query or a String
	 * @deprecated for the same reason as {@link TemplateCall.getQueryString}
	 */
	public static String getQueryString(RDFNode node, boolean usePrefixes) {
		if(node.isLiteral()) {
			return ((Literal)node).getLexicalForm();
		}
		else {
			Resource resource = (Resource)node;
			org.topbraid.spin.model.Command spinCommand = SPINFactory.asCommand(resource);
			if(spinCommand != null) {
				if(usePrefixes) {
					StringPrintContext p = new StringPrintContext();
					p.setUsePrefixes(usePrefixes);
					spinCommand.print(p);
					return p.getString();
				}
				else {
					return ARQFactory.get().createCommandString(spinCommand);
				}
			}
			else {
				TemplateCall templateCall = SPINFactory.asTemplateCall(resource);
				if(templateCall != null) {
					return templateCall.getQueryString();
				}
				else {
					throw new IllegalArgumentException("Node must be either literal or a SPIN query or a SPIN template call");
				}
			}
		}
	}
	
	
	/**
	 * Gets a Collection of all query strings defined as values of a given property.
	 * This will accept strings or SPIN expressions (including template calls).
	 * The query model is the subject's getModel().
	 * All sub-properties of property from the query model will also be queried.
	 * @param subject  the subject to get the values of
	 * @param property  the property to query
	 * @return a Set of query strings
	 * @deprecated for the same reasons as {@link TemplateCall.getQueryString}
	 */
	public static Collection<String> getQueryStrings(Resource subject, Property property) {
		JenaUtil.setGraphReadOptimization(true);
		try {
			Map<Statement,String> map = getQueryStringMap(subject, property);
			return map.values();
		}
		finally {
			JenaUtil.setGraphReadOptimization(false);
		}
	}
	
	
	/**
	 * Gets a Map of all query strings defined as values of a given property.
	 * This will accept strings or SPIN expressions (including template calls).
	 * The query model is the subject's getModel().
	 * All sub-properties of property from the query model will also be queried.
	 * The resulting Map will associate each query String with the Statement
	 * that has created it.
	 * @param subject  the subject to get the values of
	 * @param property  the property to query
	 * @return a Map of Statements to query strings
	 */
	public static Map<Statement,String> getQueryStringMap(Resource subject, Property property) {
		if(subject != null) {
			property = subject.getModel().getProperty(property.getURI());
		}
		Map<Statement,String> queryStrings = new HashMap<Statement,String>();
		Set<Resource> ps = JenaUtil.getAllSubProperties(property);
		ps.add(property);
		for(Resource p : ps) {
			StmtIterator it = property.getModel().listStatements(subject, JenaUtil.asProperty(p), (RDFNode)null);
			while(it.hasNext()) {
				Statement s = it.nextStatement();
				RDFNode object = s.getObject();
				String str = getQueryString(object, false);
				queryStrings.put(s, str);
			}
		}
		return queryStrings;
	}
	
	
	public static Set<Resource> getURIResources(Printable query) {
		final Set<Resource> results = new HashSet<Resource>();
		StringPrintContext context = new StringPrintContext() {

			@Override
			public PrintContext clone() {
				return this;
			}

			@Override
			public void printURIResource(Resource resource) {
				super.printURIResource(resource);
				results.add(resource);
			}
		};
		query.print(context);
		return results;
	}


	/**
	 * Checks whether a given Graph is a spin:LibraryOntology.
	 * This is true for the SP and SPIN namespaces, as well as any Graph that
	 * has [baseURI] rdf:type spin:LibraryOntology.
	 * @param graph  the Graph to test
	 * @param baseURI  the base URI of the Graph (to find the library ontology)
	 * @return true if graph is a library ontology
	 */
	public static boolean isLibraryOntology(Graph graph, URI baseURI) {
		if(baseURI != null) {
			if(SP.BASE_URI.equals(baseURI.toString()) || SPIN.BASE_URI.equals(baseURI.toString())) {
				return true;
			}
			else {
				Node ontology = NodeFactory.createURI(baseURI.toString());
				return graph.contains(ontology, RDF.type.asNode(), SPIN.LibraryOntology.asNode());
			}
		}
		else {
			return false;
		}
	}
	
	
	public static boolean isRootClass(Resource cls) {
		return RDFS.Resource.equals(cls) || OWL.Thing.equals(cls);
	}

	
	/**
	 * Converts a map from Properties to RDFNode values to a Map from variable
	 * names (Strings) to those values, for quicker look up.
	 * @param map  the old Map
	 * @return the new Map
	 */
	public static Map<String,RDFNode> mapProperty2VarNames(Map<Property,RDFNode> map) {
		Map<String,RDFNode> results = new HashMap<String,RDFNode>();
		for(Property predicate : map.keySet()) {
			RDFNode value = map.get(predicate);
			results.put(predicate.getLocalName(), value);
		}
		return results;
	}
}
