/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import org.topbraid.spin.model.print.Printable;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * The base interface of TriplePattern and TripleTemplate.
 * 
 * @author Holger Knublauch
 */
public interface Triple extends Printable, Resource {
	
	/**
	 * Gets the subject of this Triple, downcasting it into Variable if appropriate.
	 * @return the subject
	 */
	Resource getSubject();
	

	/**
	 * Gets the predicate of this Triple, downcasting it into Variable if appropriate.
	 * @return the predicate
	 */
	Resource getPredicate();
	
	
	/**
	 * Gets the object of this Triple, downcasting it into Variable if appropriate.
	 * @return the object
	 */
	RDFNode getObject();
	
	
	/**
	 * Gets the object as a Resource.
	 * @return the object or null if it's not a resource (i.e., a literal)
	 */
	Resource getObjectResource();
}
