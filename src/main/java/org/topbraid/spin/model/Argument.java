/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import org.apache.jena.rdf.model.RDFNode;



/**
 * Jena wrapper for instances of spl:Argument.
 * 
 * @author Holger Knublauch
 */
public interface Argument extends AbstractAttribute {
	
	
	/**
	 * If this is an ordered arg (sp:arg1, sp:arg2, ...) then this returns
	 * the index of this, otherwise null.
	 * @return the arg index or null if this does not have an index
	 */
	Integer getArgIndex();
	
	
	/**
	 * Returns any declared spl:defaultValue.
	 * @return the default value or null
	 */
	RDFNode getDefaultValue();

	
	/**
	 * Gets the variable name associated with this Argument.
	 * This is the local name of the predicate, i.e. implementations
	 * can assume that this value is not null iff getPredicate() != null.
	 * @return the variable name
	 */
	String getVarName();
}
