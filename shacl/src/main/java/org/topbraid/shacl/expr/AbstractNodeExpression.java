package org.topbraid.shacl.expr;

import java.util.Collections;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

public abstract class AbstractNodeExpression implements NodeExpression {
	
	private final static List<NodeExpression> EMPTY = Collections.emptyList();

	private RDFNode expr;
	
	
	protected AbstractNodeExpression(RDFNode expr) {
		this.expr = expr;
	}


	@Override
	public ExtendedIterator<RDFNode> evalReverse(RDFNode valueNode, NodeExpressionContext context) {
		throw new IllegalStateException("Reverse evaluation is not supported for this node expression: " + toString());
	}


	@Override
	public List<NodeExpression> getInputExpressions() {
		return EMPTY;
	}


	@Override
	public Resource getOutputShape(Resource contextShape) {
		return null;
	}


	@Override
	public RDFNode getRDFNode() {
		return expr;
	}


	@Override
	public boolean isReversible(NodeExpressionContext context) {
		return false;
	}


	@Override
	public String toString() {
		return getFunctionalSyntax();
	}
}
