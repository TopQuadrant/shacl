/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.update.impl;

import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.update.Delete;
import org.topbraid.spin.vocabulary.SP;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

@Deprecated
public class DeleteImpl extends UpdateImpl implements Delete {

	public DeleteImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	public void printSPINRDF(PrintContext p) {
		printComment(p);
		printPrefixes(p);
		p.printIndentation(p.getIndentation());
		p.printKeyword("DELETE");
		printGraphIRIs(p, "FROM");
		printTemplates(p, SP.deletePattern, null, true, null);
		printWhere(p);
	}
}
