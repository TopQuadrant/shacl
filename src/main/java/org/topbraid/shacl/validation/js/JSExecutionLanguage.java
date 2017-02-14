package org.topbraid.shacl.validation.js;

import org.topbraid.shacl.validation.ConstraintExecutor;
import org.topbraid.shacl.validation.ExecutionLanguage;
import org.topbraid.shacl.validation.Constraint;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.vocabulary.SHJS;

public class JSExecutionLanguage implements ExecutionLanguage {

	private final static JSExecutionLanguage singleton = new JSExecutionLanguage();
	
	public static JSExecutionLanguage get() {
		return singleton;
	}

	
	@Override
	public boolean canExecute(Constraint constraint, ValidationEngine validationEngine) {
		return constraint.getComponent().getValidator(SHJS.JSValidator, constraint.getContext()) != null;
	}

	
	@Override
	public ConstraintExecutor createExecutor(Constraint constraint, ValidationEngine validationEngine) {
		return new JSComponentExecutor();
	}
}
