package org.topbraid.shacl.model;

/**
 * Shared interface of SHSPARQLConstraint and SHJSConstraint.
 * 
 * @author Holger Knublauch
 */
public interface SHConstraint extends SHResource {
	
	/**
	 * Checks if this constraint has been deactivated.
	 * @return true if deactivated
	 */
	boolean isDeactivated();
}
