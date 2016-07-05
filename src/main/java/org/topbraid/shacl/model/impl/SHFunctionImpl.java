package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.topbraid.shacl.model.SHFunction;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class SHFunctionImpl extends SHParameterizableImpl implements SHFunction {
	
	public SHFunctionImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	@Override
	public String getSPARQL() {
		String result = JenaUtil.getStringProperty(this, SH.select);
		if(result != null) {
			return result;
		}
		else {
			return JenaUtil.getStringProperty(this, SH.ask);
		}
	}
}