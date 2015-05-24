/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

/**
 * A SERVICE element group.
 *
 * @author Holger Knublauch
 */
public interface Service extends ElementGroup {

	/**
	 * Gets the URI of the SPARQL end point to invoke.
	 * @return the service URI (or null if this is a Variable)
	 */
	String getServiceURI();
	
	
	/**
	 * The the variable of the SPARQL end point to invoke.
	 * @return the Variable (or null if this is a URI)
	 */
	Variable getServiceVariable();
}
