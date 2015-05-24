/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import java.util.List;


/**
 * A CONSTRUCT Query.
 * 
 * @author Holger Knublauch
 */
public interface Construct extends SolutionModifierQuery {

	/**
	 * Gets the list of TripleTemplates in the head of the query.
	 * @return the templates
	 */
	List<TripleTemplate> getTemplates();
}
