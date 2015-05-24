/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.ModuleCall;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;


public abstract class ModuleCallImpl extends AbstractSPINResourceImpl implements ModuleCall {
	
	public ModuleCallImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}
