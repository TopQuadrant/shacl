package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.topbraid.shacl.model.SHParameter;

public class SHParameterImpl extends SHPropertyConstraintImpl implements SHParameter {
	
	public SHParameterImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}


	@Override
    public String toString() {
		return "Parameter " + getVarName();
	}
}