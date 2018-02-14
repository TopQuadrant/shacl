package org.topbraid.jenax.util;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.impl.WrappedGraph;

/**
 * A WrappedGraph that allows access to its delegate with a debug-friendly public method.
 * 
 * @author Holger Knublauch
 */
public class TransparentWrappedGraph extends WrappedGraph {

	public TransparentWrappedGraph(Graph delegate) {
		super(delegate);
	}
	
	
	public Graph getDelegate() {
		return base;
	}
}
