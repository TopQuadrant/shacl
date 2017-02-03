package org.topbraid.shacl.js.model;

import org.apache.jena.graph.Node;

public class JSBlankNode extends JSTerm {

	JSBlankNode(Node node) {
		super(node);
	}

	
	@Override
	public String getTermType() {
		return JSFactory.BLANK_NODE;
	}

	
	@Override
	public String getValue() {
		return node.getBlankNodeLabel();
	}
}
