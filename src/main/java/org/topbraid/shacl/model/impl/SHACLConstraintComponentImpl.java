package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.topbraid.shacl.model.SHACLConstraintComponent;

public class SHACLConstraintComponentImpl extends SHACLParameterizableImpl implements SHACLConstraintComponent {
	
	public SHACLConstraintComponentImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}