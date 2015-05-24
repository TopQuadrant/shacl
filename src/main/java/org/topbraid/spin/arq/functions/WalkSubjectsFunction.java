package org.topbraid.spin.arq.functions;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

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
