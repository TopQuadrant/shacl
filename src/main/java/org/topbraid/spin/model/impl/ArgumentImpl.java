/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.Argument;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPL;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;


public class ArgumentImpl extends AbstractAttributeImpl implements Argument {
	
	public ArgumentImpl(Node node, EnhGraph eg) {
		super(node, eg);
	}
	
	
	public Integer getArgIndex() {
		String varName = getVarName();
		if(varName != null) {
			return SP.getArgPropertyIndex(varName);
		}
		else {
			return null;
		}
	}


	public RDFNode getDefaultValue() {
		Statement s = getProperty(SPL.defaultValue);
		if(s != null) {
			return s.getObject();
		}
		else {
			return null;
		}
	}


	public String getVarName() {
		Property argProperty = getPredicate();
		if(argProperty != null) {
			return argProperty.getLocalName();
		}
		else {
			return null;
		}
	}


	public boolean isOptional() {
		Statement s = getProperty(SPL.optional);
		if(s != null && s.getObject().isLiteral()) {
			return s.getBoolean();
		}
		else {
			return false;
		}
	}
}
