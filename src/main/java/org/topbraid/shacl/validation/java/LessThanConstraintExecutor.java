package org.topbraid.shacl.validation.java;

import org.apache.jena.sparql.expr.Expr;

class LessThanConstraintExecutor extends AbstractLessThanConstraintExecutor {

	public LessThanConstraintExecutor() {
		super(c -> c != Expr.CMP_LESS, "less than");
	}
}
