package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.topbraid.shacl.model.SHJSFunction;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class SHJSFunctionImpl extends SHParameterizableImpl implements SHJSFunction {
	
	public SHJSFunctionImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public String getFunctionName() {
		String result = JenaUtil.getStringProperty(this, SH.jsFunctionName);
		if(result == null) {
			return getLocalName();
		}
		else {
			return result;
		}
	}
}