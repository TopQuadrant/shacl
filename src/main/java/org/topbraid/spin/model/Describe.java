/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import java.util.List;

import org.apache.jena.rdf.model.Resource;


/**
 * A DESCRIBE query.
 * 
 * @author Holger Knublauch
 */
public interface Describe extends SolutionModifierQuery {

	/**
	 * Gets the result nodes of this query.  The resulting Resources will be
	 * automatically typecast into Variable if they are variables.
	 * @return a List of Resources (or Variables)
	 */
	List<Resource> getResultNodes();
}
