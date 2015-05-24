/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.constraints;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * A SimplePropertyPath of the form OP->S.
 * 
 * @author Holger Knublauch
 */
public class SubjectPropertyPath extends SimplePropertyPath {
	
	public SubjectPropertyPath(Resource object, Property predicate) {
		super(object, predicate);
	}
}
