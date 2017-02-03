package org.topbraid.shacl.js.model;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public class JSLiteral extends JSTerm {
	
	JSLiteral(Node node) {
		super(node);
	}
	
	
	public JSNamedNode getDatatype() {
		String uri = node.getLiteralDatatypeURI();
		return new JSNamedNode(NodeFactory.createURI(uri));
	}
	
	
	public String getLanguage() {
		return node.getLiteralLanguage();
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
