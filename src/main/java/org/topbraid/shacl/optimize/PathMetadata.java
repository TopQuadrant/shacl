package org.topbraid.shacl.optimize;

import org.apache.jena.graph.Node;

public class PathMetadata {
	
	private boolean inverse;

	private Node predicate;
	
	
	public PathMetadata(Node predicate, boolean inverse) {
		this.inverse = inverse;
		this.predicate = predicate;
	}


	@Override
	public boolean equals(Object obj) {
		if(obj instanceof PathMetadata) {
			return ((PathMetadata)obj).inverse == inverse &&
					((PathMetadata)obj).predicate.equals(predicate);
		}
		else {
			return super.equals(obj);
		}
	}
	
	
	public Node getPredicate() {
		return predicate;
	}


	@Override
	public int hashCode() {
		return predicate.hashCode();
	}
	
	
	public boolean isInverse() {
		return inverse;
	}
	
	
	@Override
	public String toString() {
		return "PathMetadata for " + (inverse ? "^" : "") + predicate;
	}
}
