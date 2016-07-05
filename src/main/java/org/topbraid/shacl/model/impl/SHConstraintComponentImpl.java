package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.topbraid.shacl.model.SHConstraintComponent;

public class SHConstraintComponentImpl extends SHParameterizableImpl implements SHConstraintComponent {
	
	public SHConstraintComponentImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}