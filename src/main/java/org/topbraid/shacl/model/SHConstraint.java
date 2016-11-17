package org.topbraid.shacl.model;

import java.util.List;

import org.topbraid.shacl.constraints.ConstraintExecutable;

/**
 * Shared interface of SHACLSPARQLConstraint and SHACLParameterizableConstraint.
 * 
 * @author Holger Knublauch
 */
public interface SHConstraint extends SHResource {

	List<ConstraintExecutable> getExecutables();
	
	
	/**
	 * Checks if this constraint has been deactivated.
	 * @return true if deactivated
	 */
	boolean isDeactivated();
}
