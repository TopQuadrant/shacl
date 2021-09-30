package org.topbraid.shacl.validation.java;

import org.apache.jena.sparql.expr.Expr;

/**
 * Validator for sh:minExclusive constraints.
 * 
 * @author Holger Knublauch
 */
class MinExclusiveConstraintExecutor extends AbstractClusiveConstraintExecutor {

	public MinExclusiveConstraintExecutor() {
		super(t -> Expr.CMP_LESS == t, "greater than");
	}
}
