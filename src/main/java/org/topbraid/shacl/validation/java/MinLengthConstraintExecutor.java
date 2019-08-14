package org.topbraid.shacl.validation.java;

import org.topbraid.shacl.engine.Constraint;

/**
 * Native implementation of sh:MinLengthConstraintComponent.
 * 
 * @author Holger Knublauch
 */
class MinLengthConstraintExecutor extends AbstractLengthConstraintExecutor {
	
	public MinLengthConstraintExecutor(Constraint constraint) {
		super(constraint);
	}

	
	@Override
	protected String getComparisonString() {
		return "less";
	}

	
	@Override
	protected boolean isInvalidLength(int actualLength, int expectedLength) {
		return actualLength < expectedLength;
	}
}
