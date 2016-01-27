/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.Attribute;
import org.topbraid.spin.vocabulary.SPL;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;


public class AttributeImpl extends AbstractAttributeImpl implements Attribute {
	
	public AttributeImpl(Node node, EnhGraph eg) {
		super(node, eg);
	}


	public boolean isOptional() {
		return getMinCount() == 0;
	}


	public RDFNode getDefaultValue() {
		return getRDFNode(SPL.defaultValue);
	}


	public Integer getMaxCount() {
		Statement s = getProperty(SPL.maxCount);
		if(s != null && s.getObject().isLiteral()) {
			return s.getInt();
		}
		else {
			return null;
		}
	}


	public int getMinCount() {
		Statement s = getProperty(SPL.minCount);
		if(s != null && s.getObject().isLiteral()) {
			return s.getInt();
		}
		else {
			return 0;
		}
	}
}
