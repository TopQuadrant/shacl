/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.TripleTemplate;
import org.topbraid.spin.model.print.PrintContext;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;


public class TripleTemplateImpl extends TripleImpl implements TripleTemplate {

	public TripleTemplateImpl(Node node, EnhGraph eh) {
		super(node, eh);
	}

	
	@Override
	public void print(PrintContext p) {
		p.setNamedBNodeMode(true);
		super.print(p);
		p.setNamedBNodeMode(false);
	}
}
