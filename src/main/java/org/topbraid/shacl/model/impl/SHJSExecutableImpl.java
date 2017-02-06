package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.topbraid.shacl.js.SHJS;
import org.topbraid.shacl.model.SHJSExecutable;
import org.topbraid.spin.util.JenaUtil;

public class SHJSExecutableImpl extends SHResourceImpl implements SHJSExecutable {

	public SHJSExecutableImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public String getFunctionName() {
		return JenaUtil.getStringProperty(this, SHJS.jsFunctionName);
	}
}
