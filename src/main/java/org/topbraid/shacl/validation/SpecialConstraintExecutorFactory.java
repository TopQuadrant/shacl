package org.topbraid.shacl.validation;

public interface SpecialConstraintExecutorFactory {

	ConstraintExecutor create(Constraint constraint);
}
