/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
package org.topbraid.shacl.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Node;

/**
 * A ThreadLocal structure to prevent infinite loops of tosh:hasShape calls etc.
 * 
 * @author Holger Knublauch
 */
public class RecursionGuard {
	
	private static ThreadLocal<Set<Call>> sets = new ThreadLocal<Set<Call>>();
	
	
	public static boolean start(Node focusNode, Node shape) {
		Set<Call> set = sets.get();
		if(set == null) {
			set = new HashSet<Call>();
			sets.set(set);
		}
		Call call = new Call(focusNode, shape);
		if(set.contains(call)) {
			return true;
		}
		else {
			set.add(call);
			return false;
		}
	}
	
	
	public static void end(Node focusNode, Node shape) {
		sets.get().remove(new Call(focusNode, shape));
	}
	
	
	private static class Call {
		
		private Node focusNode;
		
		private Node shape;
		
		
		Call(Node resource, Node shape) {
			this.focusNode = resource;
			this.shape = shape;
		}
		

		@Override
		public boolean equals(Object other) {
			if(other instanceof Call) {
				return ((Call)other).focusNode.equals(focusNode) && ((Call)other).shape.equals(shape);
			}
			else {
				return false;
			}
		}

		
		@Override
		public int hashCode() {
			return focusNode.hashCode() + shape.hashCode();
		}
		
		
		@Override
        public String toString() {
			return "(" + focusNode + ", " + shape + ")";
		}
	}
}
