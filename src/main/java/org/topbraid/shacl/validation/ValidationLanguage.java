package org.topbraid.shacl.validation;

import org.topbraid.shacl.engine.Constraint;

/**
 * Shared interface for SHACL-SPARQL and SHACL-JS validation, or potential other languages.
 * 
 * Managed and accessed via ValidationLanguages singleton;
 * 
 * @author Holger Knublauch
 */
public interface ValidationLanguage {

	boolean canExecute(Constraint constraint, ValidationEngine engine);
	
	ConstraintExecutor createExecutor(Constraint constraint, ValidationEngine engine);
}
