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
