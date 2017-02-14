package org.topbraid.shacl.validation;

/**
 * Shared interface for SHACL-SPARQL and SHACL-JS validation, or potential other languages.
 * 
 * Managed and accessed via ValidationLanguages singleton;
 * 
 * @author Holger Knublauch
 */
public interface ExecutionLanguage {

	boolean canExecute(Constraint constraint, ValidationEngine engine);
	
	ConstraintExecutor createExecutor(Constraint constraint, ValidationEngine engine);
}
