package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHParameterizableScope;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class SHParameterizableScopeImpl extends SHParameterizableInstanceImpl implements SHParameterizableScope {
	
	public SHParameterizableScopeImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}