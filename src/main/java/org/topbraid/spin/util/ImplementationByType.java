/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.util;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;

/**
 * Extended polymorphism support for Jena, checking whether the Node
 * has a given rdf:type. 
 * 
 * @author Holger Knublauch
 */
public abstract class ImplementationByType extends Implementation {

	private final Node type;


	public ImplementationByType(Node type) {
		this.type = type;
	}


	@Override
	public boolean canWrap(Node node, EnhGraph eg) {
		return eg.asGraph().contains(node, RDF.type.asNode(), type);
	}
}
