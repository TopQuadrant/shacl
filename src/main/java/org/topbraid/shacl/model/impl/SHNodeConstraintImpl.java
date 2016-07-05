package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.topbraid.shacl.model.SHNodeConstraint;

public class SHNodeConstraintImpl extends SHParameterizableConstraintImpl implements SHNodeConstraint {
	
	public SHNodeConstraintImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}