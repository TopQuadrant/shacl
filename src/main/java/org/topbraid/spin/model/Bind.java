/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import com.hp.hpl.jena.rdf.model.RDFNode;


/**
 * A BIND assignment element.
 * 
 * @author Holger Knublauch
 */
public interface Bind extends Element {
	
	/**
	 * Gets the SPARQL expression delivering the assigned value.
	 * @return the expression
	 */
	RDFNode getExpression();
	

	/**
	 * Gets the variable on the right hand side of the BIND.
	 * @return the Variable
	 */
	Variable getVariable();
}
