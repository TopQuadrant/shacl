package org.topbraid.shacl.validation;

public abstract class AbstractSpecialConstraintExecutorFactory implements SpecialConstraintExecutorFactory {

	@Override
	public boolean canExecute(Constraint constraint, ValidationEngine engine) {
		return true;
	}
}
