package org.topbraid.spin.arq.functions;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

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
