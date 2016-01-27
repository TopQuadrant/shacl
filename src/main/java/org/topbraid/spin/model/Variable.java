/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import java.util.Set;

import org.topbraid.spin.model.print.Printable;

import org.apache.jena.rdf.model.Resource;


/**
 * A variable in a SPIN query.
 * 
 * @author Holger Knublauch
 */
public interface Variable extends Resource, Printable {

	/**
	 * Gets the name of this variable (without the '?').
	 * @return the variable name
	 */
	String getName();
	
	
	/**
	 * Gets all TriplePatterns where this Variable is mentioned.
	 * @return the TriplePatterns
	 */
	Set<TriplePattern> getTriplePatterns();
	
	
	/**
	 * Checks if this represents a blank node var.
	 * @return true  if a blank node var
	 */
	boolean isBlankNodeVar();
}
