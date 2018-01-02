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
package org.topbraid.shacl.optimize;

/**
 * An abstraction layer that helps the OntologyOptimizations determine whether
 * it needs to invalidate caches or whether the graph can benefit from caching at all.
 * 
 * @author Holger Knublauch
 */
public interface OntologyOptimizableGraph {

	/**
	 * Gets a unique identifier for the graph that is used for caching.
	 * In TopBraid EVN this is the graph ID (of the base graph).
	 * This is only ever called after isOntologyOptimizable has been true.
	 * @return the unique key
	 */
	String getOntologyGraphKey();

	
	/**
	 * Checks if this graph is an Ontology graph, i.e. it may define classes, properties
	 * and shapes that may invalidate caches.
	 * @return true  if this is an Ontology graph
	 */
	boolean isOntologyGraph();
	
	
	/**
	 * Checks if OntologyOptimizations caches can be used for this graph.
	 * In TopBraid this is the case for TeamBufferingGraphs that are either master
	 * graphs or a working copy that is not an Ontology project.
	 * @return true  if caching should be used
	 */
	boolean isUsingOntologyOptimizations();
}
