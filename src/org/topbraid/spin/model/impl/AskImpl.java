/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.Ask;
import org.topbraid.spin.model.print.PrintContext;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;


public class AskImpl extends QueryImpl implements Ask {

	public AskImpl(Node node, EnhGraph eh) {
		super(node, eh);
	}
	
	
	public void printSPINRDF(PrintContext context) {
		printComment(context);
		printPrefixes(context);
		context.printIndentation(context.getIndentation());
		context.printKeyword("ASK");
		printStringFrom(context);
		context.print(" ");
		if(context.getIndentation() > 0) {
			// Avoid unnecessary whitespace after ASK -> put on extra row
			context.println();
		}
		printWhere(context);
		printValues(context);
	}
}
