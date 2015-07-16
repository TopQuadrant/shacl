/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.print;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.topbraid.spin.system.ExtraPrefixes;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.Util;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.NodeToLabelMap;


/**
 * A simple implementation of PrintContext that operates on a StringBuilder.
 * 
 * By default this is using prefixes but not extra prefixes.
 * 
 * @author Holger Knublauch
 */
public class StringPrintContext implements PrintContext {
	
	private static final PrefixMapping noPrefixMapping = new PrefixMappingImpl();
	
	private int indentation;
	
	private Map<String,RDFNode> initialBindings;
	
	protected String indentationString = "    ";
	
	private boolean namedBNodeMode;
	
	private boolean nested;
	
	private NodeToLabelMap nodeToLabelMap;
	
	private boolean printPrefixes;
	
	private StringBuilder sb;
	
	private boolean useExtraPrefixes;
	
	private boolean usePrefixes = true;
	
	
	public StringPrintContext() {
		this(new StringBuilder());
	}
	
	
	public StringPrintContext(StringBuilder sb) {
		this(sb, new HashMap<String,RDFNode>());
	}
	
	
	public StringPrintContext(StringBuilder sb, Map<String,RDFNode> initialBindings) {
		this.sb = sb;
		this.initialBindings = initialBindings;
	}
	
	
	public PrintContext clone() {
		StringPrintContext cl = new StringPrintContext(sb);
		cl.setIndentation(getIndentation());
		cl.setNested(isNested());
		cl.setUseExtraPrefixes(getUseExtraPrefixes());
		cl.setUsePrefixes(getUsePrefixes());
		cl.initialBindings = initialBindings;
		cl.sb = this.sb;
		return cl;
	}
	

	public int getIndentation() {
		return indentation;
	}
	
	
	@Override
	public RDFNode getInitialBinding(String varName) {
		return initialBindings.get(varName);
	}


	public NodeToLabelMap getNodeToLabelMap() {
		if(nodeToLabelMap == null) {
			nodeToLabelMap = new NodeToLabelMap();
		}
		return nodeToLabelMap;
	}


	public boolean getPrintPrefixes() {
		return printPrefixes;
	}


	public String getString() {
		return sb.toString();
	}
	
	
	public StringBuilder getStringBuilder() {
		return sb;
	}


	public boolean getUseExtraPrefixes() {
		return useExtraPrefixes;
	}
	
	
	public boolean getUsePrefixes() {
		return usePrefixes;
	}
	
	
	@Override
	public boolean hasInitialBindings() {
		return initialBindings != null && !initialBindings.isEmpty();
	}


	@Override
	public boolean isNamedBNodeMode() {
		return namedBNodeMode;
	}


	public boolean isNested() {
		return nested;
	}
	
	
	/**
	 * @param str Non-null string.
	 */
	public void print(String str) {
		sb.append(str.toString());
	}


	public void printIndentation(int depth) {
		for(int i = 0; i < depth; i++) {
			print(indentationString);
		}
	}


	public void printKeyword(String str) {
		print(str);
	}


	public void println() {
		print("\n");
	}


	public void printVariable(String str) {
		RDFNode binding = getInitialBinding(str);
		if(binding == null || binding.isAnon()) {
			print("?" + str);
		}
		else if(binding.isURIResource()) {
			printURIResource((Resource)binding);
		}
		else {
			String lit = FmtUtils.stringForNode(binding.asNode(), noPrefixMapping);
			print(lit);
		}
	}


	public void printURIResource(Resource resource) {
		if(getUsePrefixes()) {
			String qname = qnameFor(resource);
			if(qname != null) {
				print(qname);
				return;
			}
			else if(getUseExtraPrefixes()) {
				Map<String,String> extras = ExtraPrefixes.getExtraPrefixes();
				for(String prefix : extras.keySet()) {
					String ns = extras.get(prefix);
					if(resource.getURI().startsWith(ns)) {
						print(prefix);
						print(":");
						print(resource.getURI().substring(ns.length()));
						return;
					}
				}
			}
		}
		print("<");
		print(resource.getURI());
		print(">");
	}
	

	/**
	 * Work-around for a bug in Jena: Jena would use the default
	 * namespace of an imported Graph in a MultiUnion.
	 * @param resource  the Resource to get the qname for
	 * @return the qname or null
	 */
	public static String qnameFor(Resource resource) {
		Graph graph = resource.getModel().getGraph();
		if(graph instanceof MultiUnion) {
			String uri = resource.getURI();
	        int split = Util.splitNamespace(uri);
	        String local = uri.substring(split);
	        if (local.length() == 0) {
	        	return null;
	        }
	        String ns = uri.substring(0, split);
	        
	        MultiUnion mu = (MultiUnion) graph;
			Graph baseGraph = mu.getBaseGraph();
			if(baseGraph != null) {
				String prefix = baseGraph.getPrefixMapping().getNsURIPrefix(ns);
				if(prefix != null) {
					return prefix + ":" + local;
				}
			}
            List<Graph> graphs = mu.getSubGraphs();
            for (int i = 0; i < graphs.size(); i++) {
            	Graph subGraph = graphs.get(i);
            	String prefix = subGraph.getPrefixMapping().getNsURIPrefix(ns);
            	if(prefix != null && prefix.length() > 0) {
                	return prefix + ":" + local;
                }
            }
            return null;
		}
		else {
			return resource.getModel().qnameFor(resource.getURI());
		}
	}


	public void setIndentation(int value) {
		this.indentation = value;
	}
	
	
	public void setIndentationString(String value) {
		this.indentationString = value;
	}
	
	
	@Override
	public void setNamedBNodeMode(boolean value) {
		this.namedBNodeMode = value;
	}


	public void setNested(boolean value) {
		this.nested = value;
	}


	public void setPrintPrefixes(boolean value) {
		this.printPrefixes = value;
	}


	public void setUseExtraPrefixes(boolean value) {
		this.useExtraPrefixes = value;
	}
	
	
	public void setUsePrefixes(boolean value) {
		this.usePrefixes = value;
	}
}
