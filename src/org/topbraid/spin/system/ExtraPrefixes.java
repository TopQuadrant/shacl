/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.system;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Resource;


/**
 * Manages extra prefixes that are always available even if not
 * explicitly declared.  Examples include fn and Jena's afn.
 * 
 * @author Holger Knublauch
 */
public class ExtraPrefixes {

	private static Map<String,String> map = new HashMap<String,String>();
	
	static {
		map.put("afn", "http://jena.hpl.hp.com/ARQ/function#");
		map.put("fn", "http://www.w3.org/2005/xpath-functions#");
		map.put("jfn", "java:com.hp.hpl.jena.sparql.function.library.");
		map.put("pf", "http://jena.hpl.hp.com/ARQ/property#");
		map.put("smf", "http://topbraid.org/sparqlmotionfunctions#");
		map.put("tops", "http://www.topbraid.org/tops#");
	}
	
	
	/**
	 * Programmatically adds a new prefix to be regarded as an "extra"
	 * prefix.  These are prefixes that are assumed to be valid even if
	 * they haven't been declared in the current ontology.
	 * This method has no effect if the prefix was already set before.
	 * @param prefix  the prefix to add
	 * @param namespace  the namespace to add
	 */
	public static void add(String prefix, String namespace) {
		if(!map.containsKey(prefix)) {
			map.put(prefix, namespace);
		}
	}


	/**
	 * Attempts to add an extra prefix for a given Resource.
	 * This does nothing if the prefix does not exist or is empty.
	 * @param resource  the resource to get the namespace of
	 */
	public static void add(Resource resource) {
		String ns = resource.getNameSpace();
		String prefix = resource.getModel().getNsURIPrefix(ns);
		if(prefix != null && prefix.length() > 0) {
			add(prefix, ns);
		}
	}
	
	
	/**
	 * Gets a Map from prefixes to namespaces.
	 * The result should be treated as read-only.
	 * @return the extra prefixes
	 */
	public static Map<String,String> getExtraPrefixes() {
		return map;
	}
}
