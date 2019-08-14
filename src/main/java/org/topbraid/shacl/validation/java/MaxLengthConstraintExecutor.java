package org.topbraid.shacl.validation.java;

import org.topbraid.shacl.engine.Constraint;

/**
 * Native implementation of sh:MaxLengthConstraintComponent.
 * 
 * @author Holger Knublauch
 */
class MaxLengthConstraintExecutor extends AbstractLengthConstraintExecutor {
	
	MaxLengthConstraintExecutor(Constraint constraint) {
		super(constraint);
	}

	
	@Override
	protected String getComparisonString() {
		return "more";
	}

	
	@Override
	protected boolean isInvalidLength(int actualLength, int expectedLength) {
		return actualLength > expectedLength;
	}
}
