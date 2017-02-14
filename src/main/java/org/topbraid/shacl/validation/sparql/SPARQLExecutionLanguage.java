package org.topbraid.shacl.validation.sparql;

import org.topbraid.shacl.validation.Constraint;
import org.topbraid.shacl.validation.ConstraintExecutor;
import org.topbraid.shacl.validation.ExecutionLanguage;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.vocabulary.SH;

public class SPARQLExecutionLanguage implements ExecutionLanguage {

	private final static SPARQLExecutionLanguage singleton = new SPARQLExecutionLanguage();
	
	public static SPARQLExecutionLanguage get() {
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
