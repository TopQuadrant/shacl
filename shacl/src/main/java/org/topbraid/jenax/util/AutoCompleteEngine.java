package org.topbraid.jenax.util;

import java.util.List;
import java.util.function.Predicate;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;


/**
 * An interface for objects that can deliver the results of an auto-complete operation.
 * 
 * @author Holger Knublauch
 */
public interface AutoCompleteEngine {

	/**
	 * Performs an auto-complete operation.
	 * @param graph  the Graph to operate on
	 * @param typeNode  the rdf:type of the result objects
	 * @param langs  the match languages of the literals
	 * @param prefix  the prefix string that was entered
	 * @param count  the max number of results to return or -1
	 * @param offset  the offset from which to start returning values
	 * @param filter  an optional additional filter to drop results
	 * @return an ordered list of triples with the resources as subjects and their labels as objects
	 */
	List<Triple> getResults(Graph graph, Node typeNode, String[] langs, String prefix, int count, int offset, Predicate<Node> filter);
}
