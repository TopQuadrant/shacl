package org.topbraid.shacl.validation.java;

import org.apache.jena.sparql.expr.Expr;

/**
 * Validator for sh:maxExclusive constraints.
 * 
 * @author Holger Knublauch
 */
class MaxExclusiveConstraintExecutor extends AbstractClusiveConstraintExecutor {

	public MaxExclusiveConstraintExecutor() {
		super(t -> Expr.CMP_GREATER == t, "less than");
	}
}
