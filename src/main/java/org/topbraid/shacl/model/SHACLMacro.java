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
	 * Gets a List of all declared Arguments.
	 * If ordered, then the local names of the predicates are used.
	 * @param ordered  true to get an ordered list back (slower)
	 * @return the (possibly empty) List of Arguments
	 */
	List<SHACLArgument> getArguments(boolean ordered);
	

	/**
	 * Gets a Map of variable names to Arguments.
	 * @return a Map of variable names to Arguments
	 */
	Map<String,SHACLArgument> getArgumentsMap();
	
	
	/**
	 * Gets the SPARQL body (if defined).
	 * @return the body or null
	 */
	String getSPARQL();
}
