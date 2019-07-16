package org.topbraid.shacl.expr;

import java.util.Collections;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.iterator.ExtendedIterator;

public abstract class AbstractInputExpression extends ComplexNodeExpression {

	private NodeExpression input;
	
	
	protected AbstractInputExpression(RDFNode expr, NodeExpression input) {
		super(expr);
		this.input = input;
	}
	
	
	protected ExtendedIterator<RDFNode> evalInput(RDFNode focusNode, NodeExpressionContext context) {
		return input.eval(focusNode, context);
	}


	@Override
	public List<String> getFunctionalSyntaxArguments() {
		return Collections.singletonList(input.getFunctionalSyntax());
	}
	
	
	public NodeExpression getInput() {
		return input;
	}


	@Override
	public List<NodeExpression> getInputExpressions() {
		NodeExpression input = getInput();
		if(input == null) {
			return super.getInputExpressions();
		}
		else {
			return Collections.singletonList(input);
		}
	}
}