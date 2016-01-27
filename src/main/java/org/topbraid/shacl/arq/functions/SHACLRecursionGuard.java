package org.topbraid.shacl.arq.functions;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Node;

/**
 * A ThreadLocal structure to prevent infinite loops of sh:hasShape calls.
 * 
 * @author Holger Knublauch
 */
class SHACLRecursionGuard {
	
	private static ThreadLocal<Set<Call>> sets = new ThreadLocal<Set<Call>>();
	
	
	public static boolean start(Node resource, Node matchType) {
		
		Set<Call> set = sets.get();
		if(set == null) {
			set = new HashSet<Call>();
			sets.set(set);
		}

		Call call = new Call(resource, matchType);
		if(set.contains(call)) {
			// System.out.println("Recursion: " + call);
			return true;
		}
		else {
			set.add(call);
			// System.out.println("Step " + set.size() + ": " + call);
			return false;
		}
	}
	
	
	public static void end(Node resource, Node matchType) {
		sets.get().remove(new Call(resource, matchType));
	}
	
	
	private static class Call {
		
		private Node resource;
		
		private Node shape;
		
		
		Call(Node resource, Node shape) {
			this.resource = resource;
			this.shape = shape;
		}
		

		@Override
		public boolean equals(Object other) {
			if(other instanceof Call) {
				return ((Call)other).resource.equals(resource) && ((Call)other).shape.equals(shape);
			}
			else {
				return false;
			}
		}

		
		@Override
		public int hashCode() {
			return resource.hashCode() + shape.hashCode();
		}
		
		
		public String toString() {
			return "(" + resource + ", " + shape + ")";
		}
	}
}
