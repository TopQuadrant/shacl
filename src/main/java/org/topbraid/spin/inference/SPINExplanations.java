/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.inference;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;


/**
 * A service that can be used to provide "explanations" of inferred
 * triples.  This is populated by the TopSPIN engine and will keep
 * a Map from Triples to the strings of the query.
 * 
 * @author Holger Knublauch
 */
public class SPINExplanations {
	
	private Map<Triple,Node> classes = new HashMap<Triple,Node>();

	private Map<Triple,String> texts = new HashMap<Triple, String>();
	
	
	/**
	 * Stores a Triple - query assignment.
	 * @param triple  the inferred Triple
	 * @param text  the query text to associate with the triple
	 * @param cls  the class that was holding the rule
	 */
	public void put(Triple triple, String text, Node cls) {
		texts.put(triple, text);
		classes.put(triple, cls);
	}
	
	
	/**
	 * Gets the explanation text for a given inferred triple.
	 * @param triple  the Triple to explain
	 * @return the explanation or null if none found for triple
	 */
	public String getText(Triple triple) {
		return texts.get(triple);
	}
	
	
	/**
	 * Gets the class node that holds the rule that inferred a given inferred triple.
	 * @param triple  the Triple to explain
	 * @return the class or null if none found for triple
	 */
	public Node getClass(Triple triple) {
		return classes.get(triple);
	}
}
