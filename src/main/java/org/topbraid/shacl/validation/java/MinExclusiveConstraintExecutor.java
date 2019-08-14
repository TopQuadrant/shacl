package org.topbraid.shacl.validation.java;

import org.apache.jena.sparql.expr.Expr;

class MinExclusiveConstraintExecutor extends AbstractClusiveConstraintExecutor {

	public MinExclusiveConstraintExecutor() {
		super(t -> Expr.CMP_LESS == t, "greater than");
	}
}
