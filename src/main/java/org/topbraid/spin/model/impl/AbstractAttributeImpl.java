/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.AbstractAttribute;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.vocabulary.SPL;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.vocabulary.RDFS;


public abstract class AbstractAttributeImpl extends AbstractSPINResourceImpl implements AbstractAttribute {
	
	public AbstractAttributeImpl(Node node, EnhGraph eg) {
		super(node, eg);
	}

	
	public Property getPredicate() {
		Resource r = getResource(SPL.predicate);
		if(r != null && r.isURIResource()) {
			return new PropertyImpl(r.asNode(), (EnhGraph)r.getModel());
		}
		else {
			return null;
		}
	}


	public Resource getValueType() {
		return getResource(SPL.valueType);
	}
	
	
	public String getComment() {
		return getString(RDFS.comment);
	}


	public void print(PrintContext p) {
		// TODO Auto-generated method stub

	}
}
