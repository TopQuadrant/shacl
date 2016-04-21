package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.topbraid.shacl.model.SHACLInversePropertyConstraint;

public class SHACLInversePropertyConstraintImpl extends SHACLPredicateBasedConstraintImpl implements SHACLInversePropertyConstraint {
	
	public SHACLInversePropertyConstraintImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}


	@Override
    public String toString() {
		return "Inverse Property " + getVarName();
	}
}