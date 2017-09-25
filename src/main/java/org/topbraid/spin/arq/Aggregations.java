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

package org.topbraid.spin.arq;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.topbraid.spin.vocabulary.SP;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

/**
 * Manages the registered SPARQL aggregations (such as SUM).
 * These are loaded from the sp system ontology.
 * 
 * @author Holger Knublauch
 */
public class Aggregations {

	private static Map<String,Resource> name2Type = new HashMap<String,Resource>();

	private static Map<Resource,String> type2Name = new HashMap<Resource,String>();
	
	// Load types from sp system ontology
	static {
		Model model = SP.getModel();
		Iterator<Resource> it = model.listSubjectsWithProperty(RDFS.subClassOf, SP.Aggregation);
		while(it.hasNext()) {
			Resource aggType = it.next();
			String name = aggType.getProperty(RDFS.label).getString();
			register(aggType, name);
		}
	}
	
	
	/**
	 * If registered, returns the display name of a given aggregation type.
	 * @param aggType  the aggregation type, e.g. sp:Sum
	 * @return the name (e.g., "SUM") or null if not registered
	 */
	public static String getName(Resource aggType) {
		return type2Name.get(aggType);
	}
	
	
	/**
	 * If registered, returns the aggregation Resource for a given display name. 
	 * @param name  the name (e.g., "SUM")
	 * @return the type or null if not registered
	 */
	public static Resource getType(String name) {
		return name2Type.get(name);
	}
	

	/**
	 * Programatically adds a new aggregation type.  This is usually only
	 * populated from the sp system ontology, but API users may want to
	 * bypass (and extend) this mechanism.
	 * @param aggType  the type to register
	 * @param name  the display name
	 */
	public static void register(Resource aggType, String name) {
		type2Name.put(aggType, name);
		name2Type.put(name, aggType);
	}
}
