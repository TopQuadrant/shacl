/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import com.hp.hpl.jena.rdf.model.RDFNode;


/**
 * Jena wrapper for spl:Attribute.
 * 
 * @author Holger Knublauch
 */
public interface Attribute extends AbstractAttribute {
	
	/**
	 * Gets the declared default value of this attribute, as defined
	 * using spl:defaultValue.  Might be null.
	 * @return the default value
	 */
	RDFNode getDefaultValue();
	
	
	/**
	 * Gets the maximum cardinality of this attribute, if specified.
	 * This is based on spl:maxCount.  Null if unspecified.
	 * @return the maximum cardinality or null if none is given
	 */
	Integer getMaxCount();

	
	/**
	 * Gets the minimum cardinality of this attribute.
	 * This is based on spl:minCount.  Default value is 0.
	 * @return the minimum cardinality
	 */
	int getMinCount();
}
