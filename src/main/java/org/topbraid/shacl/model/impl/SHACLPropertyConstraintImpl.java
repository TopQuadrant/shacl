package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.topbraid.shacl.model.SHACLPropertyConstraint;

public class SHACLPropertyConstraintImpl extends SHACLPredicateBasedConstraintImpl implements SHACLPropertyConstraint {
	
	public SHACLPropertyConstraintImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}


	@Override
    public String toString() {
		return "Property " + getVarName();
	}
}