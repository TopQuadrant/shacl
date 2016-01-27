package org.topbraid.spin.arq.functions;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * The implementation of sh:walkObjects (and spif:walkObjects).
 * 
 * @author Holger Knublauch
 */
public class WalkObjectsFunction extends AbstractWalkFunction {


	protected ExtendedIterator<Triple> createIterator(Graph graph, Node node, Node predicate) {
		return graph.find(node, predicate, null);
	}

	
	protected Node getNext(Triple triple) {
		return triple.getObject();
	}
}
