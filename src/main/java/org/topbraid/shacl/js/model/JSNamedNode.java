package org.topbraid.shacl.js.model;

import org.apache.jena.graph.Node;

public class JSNamedNode extends JSTerm {
	
	JSNamedNode(Node node) {
		super(node);
	}

	
	@Override
	public String getTermType() {
		return JSFactory.NAMED_NODE;
	}

	
	@Override
	public String getValue() {
		return node.getURI();
	}
}
