package org.topbraid.shacl.validation.java;

import org.apache.jena.sparql.expr.Expr;

class LessThanOrEqualsConstraintExecutor extends AbstractLessThanConstraintExecutor {

	public LessThanOrEqualsConstraintExecutor() {
		super(c -> c != Expr.CMP_LESS && c != Expr.CMP_EQUAL, "less than or equal to");
	}
}
