/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.print;


/**
 * An interface for objects that can be printed into a PrintContext.
 * This is implemented by SPIN Queries and Elements.
 * 
 * @author Holger Knublauch
 */
public interface Printable {

	/**
	 * Instructs this to print itself into a given PrintContext.
	 * Implementations need to use the provided functions of p.
	 * @param p  the context
	 */
	void print(PrintContext p);
}
