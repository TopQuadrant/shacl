package org.topbraid.shacl.validation.java;

import org.apache.jena.sparql.expr.Expr;

/**
 * Validator for sh:maxInclusive constraints.
 * 
 * @author Holger Knublauch
 */
class MaxInclusiveConstraintExecutor extends AbstractClusiveConstraintExecutor {

	public MaxInclusiveConstraintExecutor() {
		super(t -> Expr.CMP_GREATER == t || Expr.CMP_EQUAL == t, "less than or equal to");
	}
}
