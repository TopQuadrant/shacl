package org.topbraid.shacl.validation.js;

import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.ConstraintExecutor;
import org.topbraid.shacl.validation.ValidationLanguage;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.vocabulary.SH;

public class JSValidationLanguage implements ValidationLanguage {

	private final static JSValidationLanguage singleton = new JSValidationLanguage();
	
	public static JSValidationLanguage get() {
		return singleton;
	}

	
	@Override
	public boolean canExecute(Constraint constraint, ValidationEngine validationEngine) {
		return constraint.getComponent().getValidator(SH.JSValidator, constraint.getContext()) != null;
	}

	
	@Override
	public ConstraintExecutor createExecutor(Constraint constraint, ValidationEngine validationEngine) {
		return new JSComponentExecutor();
	}
}
