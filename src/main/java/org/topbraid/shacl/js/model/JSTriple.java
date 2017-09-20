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
