package org.topbraid.shacl.js.model;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

public abstract class JSTerm {
	
	protected org.apache.jena.graph.Node node;
	
	
	protected JSTerm(Node node) {
		this.node = node;
	}
	
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof JSTerm) {
			return node.equals(((JSTerm)arg0).node);
		}
		else {
			return false;
		}
	}
	
	
	public JSNamedNode getDatatype() {
		String uri = node.getLiteralDatatypeURI();
		return new JSNamedNode(NodeFactory.createURI(uri));
	}
	
	
	public String getId() {
		return node.getBlankNodeLabel();
	}
	
	
	public String getLanguage() {
		return node.getLiteralLanguage();
	}
	
	
	public String getLex() {
		return node.getLiteralLexicalForm();
	}
	
	
	public org.apache.jena.graph.Node getNode() {
		return node;
	}
	
	
	public abstract String getTermType();
	
	
	public String getUri() {
		return node.getURI();
	}
	
	
	public abstract String getValue();

	
	@Override
	public int hashCode() {
		return node.hashCode();
	}
	
	
	public boolean isBlankNode() {
		return node.isBlank();
	}
	
	
	public boolean isLiteral() {
		return node.isLiteral();
	}

	
	public boolean isURI() {
		return node.isURI();
	}


	@Override
	public String toString() {
		return node.toString();
	}
}
