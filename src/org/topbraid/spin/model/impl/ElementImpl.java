/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.Element;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;


public abstract class ElementImpl extends AbstractSPINResourceImpl implements Element {
	
	public ElementImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}
