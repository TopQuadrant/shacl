package org.topbraid.shacl.js.model;

import org.apache.jena.graph.Node;

public class JSLiteral extends JSTerm {
	
	JSLiteral(Node node) {
		super(node);
	}

	
	@Override
	public String getTermType() {
		return JSFactory.LITERAL;
	}

	
	@Override
	public String getValue() {
		return node.getLiteralLexicalForm();
	}
}
