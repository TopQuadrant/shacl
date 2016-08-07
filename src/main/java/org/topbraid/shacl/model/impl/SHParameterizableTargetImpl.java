package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHParameterizableTarget;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class SHParameterizableTargetImpl extends SHParameterizableInstanceImpl implements SHParameterizableTarget {
	
	public SHParameterizableTargetImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}