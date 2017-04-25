package org.topbraid.shacl.validation;

import org.topbraid.shacl.engine.Constraint;

public abstract class AbstractSpecialConstraintExecutorFactory implements SpecialConstraintExecutorFactory {

	@Override
	public boolean canExecute(Constraint constraint, ValidationEngine engine) {
		return true;
	}
}
