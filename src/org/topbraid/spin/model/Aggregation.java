/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import org.topbraid.spin.model.print.Printable;

import com.hp.hpl.jena.rdf.model.Resource;


/**
 * Part of a SPARQL expression that calls an Aggregation (such as SUM).
 * 
 * @author Holger Knublauch
 */
public interface Aggregation extends Printable, Resource {
	
	Variable getAs();
	

	Resource getExpression();
	
	
	boolean isDistinct();
}
