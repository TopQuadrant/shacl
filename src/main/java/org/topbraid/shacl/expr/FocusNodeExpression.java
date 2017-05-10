package org.topbraid.shacl.expr;

import java.util.Collections;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;

public class FocusNodeExpression extends AtomicNodeExpression {

	@Override
	public List<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		return Collections.singletonList(focusNode);
	}

	
	@Override
	public String toString() {
		return "$this";
	}
}
