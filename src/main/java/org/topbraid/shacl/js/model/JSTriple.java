package org.topbraid.shacl.js.model;

import org.apache.jena.graph.Triple;

public class JSTriple {
	
	private Triple triple;
	
	
	JSTriple(Triple triple) {
		this.triple = triple;
	}
	
	
	public JSTerm getObject() {
		return JSFactory.asJSTerm(triple.getObject());
	}
	
	
	public JSTerm getPredicate() {
		return JSFactory.asJSTerm(triple.getPredicate());
	}
	
	
	public JSTerm getSubject() {
		return JSFactory.asJSTerm(triple.getSubject());
	}


	@Override
	public boolean equals(Object obj) {
		if(obj instanceof JSTriple) {
			return triple.equals(((JSTriple)obj).triple);
		}
		else {
			return false;
		}
	}


	@Override
	public int hashCode() {
		return triple.hashCode();
	}


	@Override
	public String toString() {
		return "Triple(" + triple.getSubject() + ", " + triple.getPredicate() + ", " + triple.getObject() + ")";
	}
}
