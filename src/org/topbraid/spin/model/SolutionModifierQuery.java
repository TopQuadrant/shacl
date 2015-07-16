/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;


/**
 * Shared functions of those Query types that can have solution modifiers.
 * 
 * @author Holger Knublauch
 */
public interface SolutionModifierQuery extends Query {

	/**
	 * Gets the LIMIT or null.
	 * @return the specified limit or null
	 */
	Long getLimit();
	
	
	/**
	 * Gets the OFFSET or null
	 * @return the specified offset or null
	 */
	Long getOffset();
}
