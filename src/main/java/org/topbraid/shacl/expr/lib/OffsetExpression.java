package org.topbraid.shacl.expr.lib;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.topbraid.shacl.expr.AbstractInputExpression;
import org.topbraid.shacl.expr.AppendContext;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionVisitor;
import org.topbraid.shacl.expr.SNEL;

public class OffsetExpression extends AbstractInputExpression {
	
	private int offset;

	
	public OffsetExpression(RDFNode expr, NodeExpression input, int offset) {
		super(expr, input);
		this.offset = offset;
	}
	
	
	@Override
	public void appendSPARQL(AppendContext context, String targetVarName) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	
	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		ExtendedIterator<RDFNode> it = evalInput(focusNode, context);
		for(int i = 0; i < offset && it.hasNext(); i++) {
			it.next();
		}
		return it;
	}

	
	@Override
	public List<String> getFunctionalSyntaxArguments() {
		return Arrays.asList(getInput().getFunctionalSyntax(), "" + offset);
	}
	
	
	@Override
	public Resource getOutputShape(Resource contextShape) {
		return getInput().getOutputShape(contextShape);
	}

	
	public int getOffset() {
		return offset;
	}
	
	
	@Override
	public SNEL getTypeId() {
		return SNEL.offset;
	}
	
	
	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
