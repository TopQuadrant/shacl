package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.topbraid.shacl.model.SHACLFunction;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class SHACLFunctionImpl extends SHACLParameterizableImpl implements SHACLFunction {
	
	public SHACLFunctionImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	@Override
	public String getSPARQL() {
		return JenaUtil.getStringProperty(this, SH.sparql);
	}
}