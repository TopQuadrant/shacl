/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.shacl.model;

import java.util.List;
import java.util.Map;


/**
 * Instances of sh:Macro (or subclasses thereof).
 * 
 * @author Holger Knublauch
 */
public interface SHACLMacro extends SHACLClass {
	
	/**
	 * Gets an unordered List of all declared Arguments.
	 * @return the (possibly empty) List of Arguments
	 */
	List<SHACLArgument> getArguments();
	

	/**
	 * Gets a Map of variable names to Arguments.
	 * @return a Map of variable names to Arguments
	 */
	Map<String,SHACLArgument> getArgumentsMap();

	
	/**
	 * Gets an ordered List of all declared Arguments, based on
	 * the local names of the predicates and sh:index values.
	 * @return the (possibly empty) List of Arguments
	 */
	List<SHACLArgument> getOrderedArguments();
	
	
	/**
	 * Gets the SPARQL body (if defined).
	 * @return the body or null
	 */
	String getSPARQL();
}
