package org.topbraid.spin.arq.functions;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * The implementation of sh:walkSubjects.
 * 
 * @author Holger Knublauch
 */
public class WalkSubjectsFunction extends AbstractWalkFunction {

	protected ExtendedIterator<Triple> createIterator(Graph graph, Node node, Node predicate) {
		return graph.find(null, predicate, node);
	}

	
	protected Node getNext(Triple triple) {
		return triple.getSubject();
	}
}
