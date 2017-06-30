package org.topbraid.spin.util;

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
