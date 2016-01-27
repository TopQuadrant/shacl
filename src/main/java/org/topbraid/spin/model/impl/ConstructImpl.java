/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import java.util.LinkedList;
import java.util.List;

import org.topbraid.spin.model.Construct;
import org.topbraid.spin.model.TripleTemplate;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.vocabulary.SP;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;


public class ConstructImpl extends QueryImpl implements Construct {

	public ConstructImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	public List<TripleTemplate> getTemplates() {
		List<TripleTemplate> results = new LinkedList<TripleTemplate>();
		for(RDFNode next : getList(SP.templates)) {
			if(next != null && next.isResource()) {
				results.add(next.as(TripleTemplate.class));
			}
		}
		return results;
	}


	public void printSPINRDF(PrintContext context) {
		printComment(context);
		printPrefixes(context);
		context.printIndentation(context.getIndentation());
		context.printKeyword("CONSTRUCT");
		context.print(" {");
		context.println();
		for(TripleTemplate template : getTemplates()) {
			context.printIndentation(context.getIndentation() + 1);
			template.print(context);
			context.print(" .");
			context.println();
		}
		context.printIndentation(context.getIndentation());
		context.print("}");
		printStringFrom(context);
		context.println();
		printWhere(context);
		printSolutionModifiers(context);
		printValues(context);
	}
}
