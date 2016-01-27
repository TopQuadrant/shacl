/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import org.topbraid.spin.util.JenaUtil;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.ResourceImpl;


/**
 * Base implementation of SPINResource.
 * This is not in the impl package because the impl package is not
 * part of the public API.
 * 
 * @author Holger Knublauch
 */
public class SPINResourceImpl extends ResourceImpl implements SPINResource {

	
	public SPINResourceImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	
	
	public Integer getInteger(Property predicate) {
		return JenaUtil.getIntegerProperty(this, predicate);
	}

	
	public Literal getLiteral(Property predicate) {
		Statement s = getProperty(predicate);
		if(s != null && s.getObject().isLiteral()) {
			return s.getLiteral();
		}
		else {
			return null;
		}
	}


	public Long getLong(Property predicate) {
		Literal literal = getLiteral(predicate);
		if(literal != null) {
			return literal.getLong();
		}
		else {
			return null;
		}
	}


	public RDFNode getRDFNode(Property predicate) {
		Statement s = getProperty(predicate);
		if(s != null) {
			return s.getObject();
		}
		else {
			return null;
		}
	}


	public Resource getResource(Property predicate) {
		Statement s = getProperty(predicate);
		if(s != null && s.getObject().isResource()) {
			return s.getResource();
		}
		else {
			return null;
		}
	}


	public String getString(Property predicate) {
		Statement s = getProperty(predicate);
		if(s != null && s.getObject().isLiteral()) {
			return s.getString();
		}
		else {
			return null;
		}
	}


	public RDFNode inferRDFNode(Property predicate) {
		RDFNode existing = getRDFNode(predicate);
		if(existing != null) {
			return existing;
		}
		else {
			return null;
		}
	}
}
