/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.topbraid.jenax.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;


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
		map.put("jfn", "java:org.apache.jena.sparql.function.library.");
		map.put("list", "http://jena.apache.org/ARQ/list#");
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
	 * Creates a PrefixMapping that uses the prefixes from a Model plus any extra prefixes
	 * (unless they overlap with those from the Model).
	 * @param model  the Model to construct the PrefixMapping from
	 * @return a new PrefixMapping instance
	 */
	public static PrefixMapping createPrefixMappingWithExtraPrefixes(Model model) {
		PrefixMapping pm = new PrefixMappingImpl();

		String defaultNamespace = JenaUtil.getNsPrefixURI(model, "");
	    if(defaultNamespace != null) {
	        pm.setNsPrefix("", defaultNamespace);
	    }
	    Map<String,String> extraPrefixes = ExtraPrefixes.getExtraPrefixes();
	    for(String prefix : extraPrefixes.keySet()) {
	    	String ns = extraPrefixes.get(prefix);
	    	if(ns != null && pm.getNsPrefixURI(prefix) == null) {
	    		pm.setNsPrefix(prefix, ns);
	    	}
	    }

	    // Get all the prefixes from the model at once.
	    Map<String, String> map = model.getNsPrefixMap();
	    map.remove("");
	    pm.setNsPrefixes(map);
	    
		return pm;
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
