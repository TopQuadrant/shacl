/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * Shared base class for Argument and Attribute.
 * 
 * @author Holger Knublauch
 */
public abstract interface AbstractAttribute extends Resource {
	
	/**
	 * Gets the description (stored in rdfs:comment) of this.
	 * @return the description (if any exists)
	 */
	String getComment();

	
	/**
	 * Gets the specified sp:argProperty (if any).
	 * @return the argProperty
	 */
	Property getPredicate();
	

	/**
	 * Gets the specified spl:valueType (if any).
	 * @return the value type
	 */
	Resource getValueType();
	
	
	/**
	 * Checks if this argument has been declared to be optional.
	 * For Arguments this means spl:optional = true.
	 * For Attributes this means spl:minCardinality = 0
	 * @return  true if optional
	 */
	boolean isOptional();
}
