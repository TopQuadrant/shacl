/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.topbraid.spin.model.Describe;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.vocabulary.SP;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;


public class DescribeImpl extends QueryImpl implements Describe {

	public DescribeImpl(Node node, EnhGraph eh) {
		super(node, eh);
	}

	
	public List<Resource> getResultNodes() {
		List<Resource> results = new LinkedList<Resource>();
		for(RDFNode node : getList(SP.resultNodes)) {
			Variable variable = SPINFactory.asVariable(node);
			if(variable != null) {
				results.add(variable);
			}
			else if(node.isURIResource()) {
				results.add((Resource)node);
			}
		}
		return results;
	}
	
	
	public void printSPINRDF(PrintContext context) {
		printComment(context);
		printPrefixes(context);
		context.printKeyword("DESCRIBE");
		context.print(" ");
		List<Resource> nodes = getResultNodes();
		if(nodes.isEmpty()) {
			context.print("*");
		}
		else {
			for(Iterator<Resource> nit = nodes.iterator(); nit.hasNext(); ) {
				Resource node = nit.next();
				if(node instanceof Variable) {
					context.print(node.toString());
				}
				else {
					printVarOrResource(context, node);
				}
				if(nit.hasNext()) {
					context.print(" ");
				}
			}
		}
		printStringFrom(context);
		if(!getWhereElements().isEmpty()) {
			context.println();
			printWhere(context);
		}
		printSolutionModifiers(context);
		printValues(context);
	}
}
