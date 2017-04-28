package org.topbraid.shacl.expr;

import java.util.List;

import org.apache.jena.rdf.model.RDFNode;

public interface NodeExpression {

	// Note: the result List must not contain duplicates
	List<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context);
}
