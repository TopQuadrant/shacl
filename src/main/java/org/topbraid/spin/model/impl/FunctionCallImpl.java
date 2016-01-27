/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.topbraid.spin.model.Function;
import org.topbraid.spin.model.FunctionCall;
import org.topbraid.spin.model.Module;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPIN;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;


public class FunctionCallImpl extends ModuleCallImpl implements FunctionCall {
	
	private static final String SP_ARG = SP.arg.getURI();
	
	
	public FunctionCallImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	
	
	public List<RDFNode> getArguments() {
		Map<Property,RDFNode> values = getArgumentsMap();
		Property[] ps = getArgumentProperties(values);
		List<RDFNode> results = new ArrayList<RDFNode>(ps.length);
		for(Property key : ps) {
			RDFNode node = values.get(key);
			results.add(SPINFactory.asExpression(node));
		}
		return results;
	}


	private Property[] getArgumentProperties(Map<Property, RDFNode> values) {
		Property[] ps = new Property[values.size()];
		List<Property> others = new LinkedList<Property>();
		for(Property p : values.keySet()) {
			if(p.getURI().startsWith(SP_ARG) && !p.equals(SP.arg)) {
				int index = Integer.valueOf(p.getURI().substring(SP_ARG.length()));
				ps[index - 1] = p;
			}
			else {
				others.add(p);
			}
		}
		if(!others.isEmpty()) {
			Collections.sort(others, new Comparator<Property>() {
				@Override
				public int compare(Property arg0, Property arg1) {
					return arg0.getLocalName().compareTo(arg1.getLocalName());
				}
			});
			Iterator<Property> it = others.iterator();
			for(int i = 0; i < ps.length; i++) {
				if(ps[i] == null) {
					ps[i] = it.next();
				}
			}
		}
		return ps;
	}
	
	
	public Map<Property, RDFNode> getArgumentsMap() {
		final Map<Property,RDFNode> values = new HashMap<Property,RDFNode>();
		StmtIterator it = listProperties();
		while(it.hasNext()) {
			Statement s = it.next();
			if(!RDF.type.equals(s.getPredicate())) {
				values.put(s.getPredicate(), s.getObject());
			}
		}
		return values;
	}
	
	
	public Resource getFunction() {
		
		// Need to iterate over rdf:types - some may have been inferred
		// Return the most specific type, i.e. the one that does not have
		// any subclasses
		Resource type = null;
		StmtIterator it = listProperties(RDF.type);
		while(it.hasNext()) {
			Statement s = it.next();
			if(s.getObject().isURIResource()) {
				Resource candidate = s.getResource();
				if(type == null) {
					type = candidate;
				}
				else if(!getModel().contains(null, RDFS.subClassOf, candidate)) {
					type = candidate;
				}
			}
		}
		
		if(type != null) {
			if(JenaUtil.hasIndirectType(type, SPIN.Function.inModel(type.getModel()))) {
				return type;
			}
			else {
				Resource global = SPINModuleRegistry.get().getFunction(type.getURI(), null);
				if(global != null) {
					return global;
				}
				else {
					return type;
				}
			}
		}
		else {
			return null;
		}
	}
	
	
	@Override
	public Module getModule() {
		Resource function = getFunction();
		if(function != null) {
			return function.as(Function.class);
		}
		else {
			return null;
		}
	}


	private String getSymbol(Resource function) {
		if(function != null) {
			Statement s = function.getProperty(SPIN.symbol);
			if(s != null && s.getObject().isLiteral()) {
				return s.getString();
			}
		}
		return null;
	}
	
	
	public static boolean isSetOperator(String symbol) {
		return "IN".equals(symbol) || "NOT IN".equals(symbol);
	}
	
	
	public void print(PrintContext p) {
		Resource function = getFunction();
		List<RDFNode> args = getArguments();
		
		String symbol = getSymbol(function);
		if(symbol != null && (!Character.isLetter(symbol.charAt(0)) || isSetOperator(symbol))) {
			printOperator(p, symbol, args);
		}
		else if(symbol != null && (SP.exists.equals(function) || SP.notExists.equals(function))) {
			printExistsOrNotExists(p, symbol);
		}
		else {
			printFunction(p, function, args);
		}
	}


	void printOperator(PrintContext p, String operator, List<RDFNode> args) {
		if(p.isNested()) {
			p.print("(");
		}
		boolean set = isSetOperator(operator);
		if(args.size() == 1 && !set) {
			p.print(operator);
			printNestedExpressionString(p, args.get(0));
		}
		else { // assuming params.size() == 2
			printNestedExpressionString(p, args.get(0));
			p.print(" ");
			p.print(operator);
			p.print(" ");
			if(set) {
				p.print("(");
				for(int i = 1; i < args.size(); i++) {
					if(i > 1) {
						p.print(", ");
					}
					RDFNode arg = args.get(i);
					printNestedExpressionString(p, arg);
				}
				p.print(")");
			}
			else {
				printNestedExpressionString(p, args.get(1));
			}
		}
		if(p.isNested()) {
			p.print(")");
		}
	}


	void printExistsOrNotExists(PrintContext p, String symbol) {
		p.print(symbol);
		printNestedElementList(p, SP.elements);
	}


	void printFunction(PrintContext p, Resource function, List<RDFNode> args) {
		printFunctionQName(p, function);
		p.print("(");
		Iterator<RDFNode> it = args.iterator();
		while(it.hasNext()) {
			RDFNode param = it.next();
			printNestedExpressionString(p, param);
			if(it.hasNext()) {
				p.print(", ");
			}
		}
		p.print(")");
	}


	private void printFunctionQName(PrintContext p, Resource function) {
		String symbol = getSymbol(function);
		if(symbol != null) {
			p.print(symbol);
		}
		else {
			String uri = function.getURI();
			p.printURIResource(getModel().getResource(uri));
		}
	}
}
