/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import org.topbraid.spin.model.print.Printable;

import org.apache.jena.rdf.model.Resource;

/**
 * Represents instances of sp:Command (Queries or Update requests).
 *
 * @author Holger Knublauch
 */
public interface Command extends Printable, Resource {
	
	/**
	 * Gets the comment if any has been stored as rdfs:comment.
	 * @return the comment or null
	 */
	String getComment();
}
