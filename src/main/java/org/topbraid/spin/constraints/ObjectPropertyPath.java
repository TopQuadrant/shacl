/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.constraints;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;


/**
 * A SimplePropertyPath of the form SP->O.
 * 
 * @author Holger Knublauch
 */
public class ObjectPropertyPath extends SimplePropertyPath {
	
	public ObjectPropertyPath(Resource subject, Property predicate) {
		super(subject, predicate);
	}
}
