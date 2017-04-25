package org.topbraid.shacl.validation;

import org.topbraid.shacl.engine.Constraint;

public interface SpecialConstraintExecutorFactory {
	
	boolean canExecute(Constraint constraint, ValidationEngine engine);

	ConstraintExecutor create(Constraint constraint);
}
