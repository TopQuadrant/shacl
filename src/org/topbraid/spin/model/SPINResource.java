/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * An extension of the Jena Resource interface with additional
 * convenience methods to easier access property values. 
 * 
 * @author Holger Knublauch
 */
public interface SPINResource extends Resource {

	/**
	 * Gets the "first" property value of this but only if it's a Literal.
	 * Returns null if the value is a Resource. 
	 * @param predicate  the predicate
	 * @return the "first" property value or null
	 */
	Literal getLiteral(Property predicate);
	
	
	Long getLong(Property predicate);
	
	
	RDFNode getRDFNode(Property predicate);
	
	
	Resource getResource(Property predicate);
	
	
	String getString(Property predicate);
}
