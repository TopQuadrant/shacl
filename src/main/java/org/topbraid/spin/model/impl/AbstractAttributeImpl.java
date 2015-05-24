/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.AbstractAttribute;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.vocabulary.SPL;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.vocabulary.RDFS;


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
