package org.topbraid.shacl.validation.sparql;

import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.ConstraintExecutor;
import org.topbraid.shacl.validation.ValidationLanguage;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.vocabulary.SH;

public class SPARQLValidationLanguage implements ValidationLanguage {

	private final static SPARQLValidationLanguage singleton = new SPARQLValidationLanguage();
	
	public static SPARQLValidationLanguage get() {
		return singleton;
	}

	
	@Override
	public boolean canExecute(Constraint constraint, ValidationEngine engine) {
		return constraint.getComponent().getValidator(SH.SPARQLExecutable, constraint.getContext()) != null;
	}

	
	@Override
	public ConstraintExecutor createExecutor(Constraint constraint, ValidationEngine engine) {
		return new SPARQLComponentExecutor(constraint);
	}
}
