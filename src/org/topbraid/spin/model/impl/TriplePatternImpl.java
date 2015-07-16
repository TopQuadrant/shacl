/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.TriplePattern;
import org.topbraid.spin.model.visitor.ElementVisitor;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;


public class TriplePatternImpl extends TripleImpl implements TriplePattern {

	public TriplePatternImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}


	public void visit(ElementVisitor visitor) {
		visitor.visit(this);
	}
}
