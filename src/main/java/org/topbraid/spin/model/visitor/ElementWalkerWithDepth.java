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
package org.topbraid.spin.model.visitor;

import org.topbraid.spin.model.ElementGroup;

/**
 * An ElementWalker that also keeps track of the depth inside of the element
 * structure.  This can be used to determine whether the currently visited
 * element is somewhere nested inside of other elements.
 * 
 * @author Holger Knublauch
 */
public class ElementWalkerWithDepth extends ElementWalker {

	private int depth;
	
	
	public ElementWalkerWithDepth(ElementVisitor elementVisitor, ExpressionVisitor expressionVisitor) {
		super(elementVisitor, expressionVisitor);
	}
	
	
	public int getDepth() {
		return depth;
	}


	@Override
	protected void visitChildren(ElementGroup group) {
		depth++;
		super.visitChildren(group);
		depth--;
	}
}
