package org.topbraid.shacl.model;

import java.util.List;

import org.topbraid.shacl.constraints.ConstraintExecutable;

/**
 * Shared interface of SHACLNativeConstraint and SHACLTemplateConstraint.
 * 
 * @author Holger Knublauch
 */
public interface SHACLConstraint extends SHACLResource {

	List<ConstraintExecutable> getExecutables();
}
