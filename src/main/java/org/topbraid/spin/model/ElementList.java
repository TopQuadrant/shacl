/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import org.apache.jena.rdf.model.RDFList;

/**
 * An RDFList representing a plain list of sub-Elements in a Query.
 * Example:
 * 
 * ASK WHERE {
 *     {
 *         ?this is:partOf :ElementList
 * 	   } 
 * }
 * 
 * @author Holger Knublauch
 */
public interface ElementList extends ElementGroup, RDFList {
}
