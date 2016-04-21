package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLParameterizableScope;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class SHACLParameterizableScopeImpl extends SHACLParameterizableInstanceImpl implements SHACLParameterizableScope {
	
	public SHACLParameterizableScopeImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}