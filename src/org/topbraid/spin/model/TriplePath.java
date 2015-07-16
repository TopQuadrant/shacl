/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * A triple path element.
 * 
 * @author Holger Knublauch
 */
public interface TriplePath extends Element {

	/**
	 * Gets the subject.
	 * @return the subject
	 */
	Resource getSubject();
	
	
	/**
	 * Gets the object.
	 * @return the object
	 */
	RDFNode getObject();
}
