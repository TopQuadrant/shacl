/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * A Resource that also may have spin constraints or rules attached to it.
 * This is basically a convenience layer that can be used to access those
 * constraints and rules more easily.
 * 
 * @author Holger Knublauch
 */
public interface SPINInstance extends Resource {

	/**
	 * Gets the queries and template calls associated with this.
	 * @param predicate  the predicate such as <code>spin:rule</code>
	 * @return a List of QueryOrTemplateCall instances
	 */
	List<QueryOrTemplateCall> getQueriesAndTemplateCalls(Property predicate);
}
