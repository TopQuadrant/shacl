/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import java.util.List;


/**
 * A collection of zero or more child Elements.
 * Implementations include Optional, Union etc.
 * 
 * @author Holger Knublauch
 */
public interface ElementGroup extends Element {

	/**
	 * Gets the List of child Elements.
	 * @return a List of children
	 */
	List<Element> getElements();
}
