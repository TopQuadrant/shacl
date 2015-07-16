/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.Function;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;


public class FunctionImpl extends ModuleImpl implements Function {
	
	public FunctionImpl(Node node, EnhGraph eg) {
		super(node, eg);
	}

	
	public Resource getReturnType() {
		return getResource(SPIN.returnType);
	}


	@Override
	public boolean isMagicProperty() {
		return JenaUtil.hasIndirectType(this, SPIN.MagicProperty);
	}


	@Override
	public boolean isPrivate() {
		return hasProperty(SPIN.private_, JenaDatatypes.TRUE);
	}
}
