/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;


/**
 * A nested sub-query.  Right now, only SELECT queries seem to be allowed
 * but this might change in the future.
 * 
 * @author Holger Knublauch
 */
public interface SubQuery extends Element {

	/**
	 * Gets the nested query.
	 * @return the query
	 */
	Query getQuery();
}
