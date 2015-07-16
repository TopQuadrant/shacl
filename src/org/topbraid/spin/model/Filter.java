/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import com.hp.hpl.jena.rdf.model.RDFNode;


/**
 * A SPARQL FILTER element.
 * 
 * @author Holger Knublauch
 */
public interface Filter extends Element {

	/**
	 * Gets the expression representing the filter condition.
	 * The result object will be typecast into the most specific
	 * subclass of RDFNode, e.g. FunctionCall or Variable.
	 * @return the expression or null
	 */
	RDFNode getExpression();
}
