/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.util;

import java.lang.reflect.Constructor;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.enhanced.EnhNode;
import com.hp.hpl.jena.enhanced.Implementation;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * An extension of the Jena polymorphism mechanism.
 * In contrast to SimpleImplementation, this maps to two different RDF classes.
 * 
 * @author Holger Knublauch
 */
public class SimpleImplementation2 extends Implementation {

	@SuppressWarnings("rawtypes")
	private Constructor constructor;

	private final Node type1;
	
	private final Node type2;


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SimpleImplementation2(Node type1, Node type2, Class implClass) {
		this.type1 = type1;
		this.type2 = type2;
		try {
			constructor = implClass.getConstructor(Node.class, EnhGraph.class);
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
	}


	@Override
	public boolean canWrap(Node node, EnhGraph eg) {
		return 	eg.asGraph().contains(node, RDF.type.asNode(), type1) ||
				eg.asGraph().contains(node, RDF.type.asNode(), type2);
	}


	@Override
	public EnhNode wrap(Node node, EnhGraph eg) {
		try {
			return (EnhNode)constructor.newInstance(node, eg);
		}
		catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}
}
