package org.topbraid.shacl.validation.java;

import org.apache.jena.sparql.expr.Expr;

/**
 * Validator for sh:lessThan constraints.
 * 
 * @author Holger Knublauch
 */
class LessThanConstraintExecutor extends AbstractLessThanConstraintExecutor {

	public LessThanConstraintExecutor() {
		super(c -> c != Expr.CMP_LESS, "less than");
	}
}
