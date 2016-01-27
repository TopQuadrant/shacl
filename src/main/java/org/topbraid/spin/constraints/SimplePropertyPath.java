/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.constraints;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;


/**
 * A property path that describes a mechanism to get values starting
 * from a given RDF node (root) by following a given predicate.
 * There are two subclasses for SP->O and OP->S paths.
 * 
 * @author Holger Knublauch
 */
public abstract class SimplePropertyPath {

	private Property predicate;
	
	private Resource root;
	
	
	public SimplePropertyPath(Resource root, Property predicate) {
		this.predicate = predicate;
		this.root = root;
	}
	
	
	public Property getPredicate() {
		return predicate;
	}
	
	
	public Resource getRoot() {
		return root;
	}
}
