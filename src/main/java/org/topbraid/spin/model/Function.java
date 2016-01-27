/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import org.apache.jena.rdf.model.Resource;


/**
 * A SPIN Function module (not: FunctionCall).
 * 
 * @author Holger Knublauch
 */
public interface Function extends Module {
	
	/**
	 * Gets the value of the spin:returnType property, if any.
	 * @return the return type or null
	 */
	Resource getReturnType();
	
	
	/**
	 * Checks if this function is a magic property, marked by having
	 * rdf:type spin:MagicProperty.
	 * @return true  if this is a magic property
	 */
	boolean isMagicProperty();
	
	
	/**
	 * Indicates if spin:private is set to true for this function.
	 * @return true  if marked private
	 */
	boolean isPrivate();
}
