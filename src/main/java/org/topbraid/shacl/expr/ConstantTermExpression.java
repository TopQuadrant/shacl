package org.topbraid.shacl.expr;

import java.util.Collections;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.util.ExprUtils;

public class ConstantTermExpression implements NodeExpression {
	
	private List<RDFNode> result;
	
	private RDFNode term;
	
	public ConstantTermExpression(RDFNode term) {
		this.result = Collections.singletonList(term);
		this.term = term;
	}

	
	@Override
	public List<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		return result;
	}

	
	@Override
	public String toString() {
		return ExprUtils.fmtSPARQL(ExprUtils.nodeToExpr(term.asNode()));
	}
}
