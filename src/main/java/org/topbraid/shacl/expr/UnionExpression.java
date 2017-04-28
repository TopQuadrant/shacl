package org.topbraid.shacl.expr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;

public class UnionExpression implements NodeExpression {
	
	private List<NodeExpression> inputs;
	
	
	public UnionExpression(List<NodeExpression> inputs) {
		this.inputs = inputs;
	}

	
	@Override
	public List<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		Set<RDFNode> results = new HashSet<RDFNode>();
		for(NodeExpression input : inputs) {
			results.addAll(input.eval(focusNode, context));
		}
		return new ArrayList<RDFNode>(results);
	}
}
