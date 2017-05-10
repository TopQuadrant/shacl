package org.topbraid.shacl.expr;

import java.util.List;

import org.apache.jena.rdf.model.RDFNode;

public abstract class NodeExpression {

	// Note: the result List must not contain duplicates
	public abstract List<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context);
}
