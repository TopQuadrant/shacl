/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.util;

import java.lang.reflect.Constructor;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.enhanced.EnhNode;
import com.hp.hpl.jena.graph.Node;

/**
 * An extension of the Jena polymorphism mechanism.
 * 
 * @author Holger Knublauch
 */
public class SimpleImplementation extends ImplementationByType {

	@SuppressWarnings("rawtypes")
	private Constructor constructor;


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SimpleImplementation(Node type, Class implClass) {
		super(type);
		try {
			constructor = implClass.getConstructor(Node.class, EnhGraph.class);
		}
		catch (Throwable t) {
			t.printStackTrace();
		}
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
