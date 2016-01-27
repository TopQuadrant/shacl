/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.Optional;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.visitor.ElementVisitor;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;


public class OptionalImpl extends ElementImpl implements Optional {
	
	public OptionalImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}


	public void print(PrintContext p) {
		p.printKeyword("OPTIONAL");
		printNestedElementList(p);
	}


	public void visit(ElementVisitor visitor) {
		visitor.visit(this);
	}
}
