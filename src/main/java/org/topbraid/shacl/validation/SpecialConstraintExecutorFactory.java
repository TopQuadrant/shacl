package org.topbraid.shacl.validation;

public interface SpecialConstraintExecutorFactory {
	
	boolean canExecute(Constraint constraint, ValidationEngine engine);

	ConstraintExecutor create(Constraint constraint);
}
