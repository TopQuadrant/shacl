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

package org.topbraid.spin.system;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;

/**
 * A singleton used by the evaluation of magic properties
 * (SPINARQPFunction) to determine whether asserted values
 * and/or dynamically computed values shall be returned in
 * SPARQL queries.  Applications can replace the singleton
 * and override the default behavior, which is to return
 * both the existing triples and the dynamically computed ones.
 * This allows applications to control caches of pre-computed
 * values to avoid costly function calls.
 *
 * @author Holger Knublauch
 */
public class MagicPropertyPolicy {

	private static MagicPropertyPolicy singleton = new MagicPropertyPolicy();
	
	public static MagicPropertyPolicy get() {
		return singleton;
	}
	
	public static void set(MagicPropertyPolicy value) {
		singleton = value;
	}
	
	
	public enum Policy {
		TRIPLES_ONLY, QUERY_RESULTS_ONLY, BOTH
	};
	
	
	/**
	 * Checks whether a given magic property call should return asserted
	 * triples, dynamically computed query results, or both for a given
	 * subject/object combination.  
	 * @param functionURI  the URI of the function
	 * @param graph  the Graph to query
	 * @param matchSubject  the subject Node or null
	 * @param matchObject  the object Node or null
	 * @return the Policy
	 */
	public Policy getPolicy(String functionURI, Graph graph, Node matchSubject, Node matchObject) {
		return Policy.QUERY_RESULTS_ONLY;
	}
}
