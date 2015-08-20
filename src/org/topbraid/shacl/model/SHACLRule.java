package org.topbraid.shacl.model;

import java.util.List;


import org.topbraid.shacl.rules.RuleExecutable;

/**
 * Shared interface of SHACLNativeConstraint and SHACLTemplateConstraint.
 * 
 * @author Holger Knublauch
 */
public interface SHACLRule extends SHACLResource {

	List<RuleExecutable> getExecutables();
}
