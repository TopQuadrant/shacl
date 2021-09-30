package org.topbraid.shacl.validation.java;

import org.topbraid.shacl.engine.Constraint;

/**
 * Validator for sh:minLength constraints.
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
