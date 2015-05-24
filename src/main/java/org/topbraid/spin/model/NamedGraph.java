/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import com.hp.hpl.jena.rdf.model.Resource;


/**
 * A named graph element (GRAPH keyword in SPARQL).
 * 
 * @author Holger Knublauch
 */
public interface NamedGraph extends ElementGroup {

	/**
	 * Gets the URI Resource or Variable that holds the name of this
	 * named graph.  If it's a Variable, then this method will typecast
	 * it into an instance of Variable.
	 * @return a Resource or Variable
	 */
	Resource getNameNode();
}
