package org.topbraid.shacl.expr.lib;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.shacl.expr.AbstractInputExpression;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionVisitor;

/**
 * Implements support for sh:if.
 * 
 * This node expression type is not part of the SHACL-AF 1.0 document, but a candidate for 1.1.
 * 
 * @author Holger Knublauch
 */
public class IfExpression extends AbstractInputExpression {
	
	private NodeExpression else_;
	
	private NodeExpression then;

	
	public IfExpression(RDFNode expr, NodeExpression input, NodeExpression then, NodeExpression else_) {
		super(expr, input);
		this.then = then;
		this.else_ = else_;
	}

	
	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		ExtendedIterator<RDFNode> it = evalInput(focusNode, context);
		try {
			if(it.hasNext() && JenaDatatypes.TRUE.equals(it.next()) && !it.hasNext()) {
				if(then != null) {
					return then.eval(focusNode, context);
				}
			}
			else {
				if(else_ != null) {
					return else_.eval(focusNode, context);
				}
			}
			return WrappedIterator.emptyIterator();
		}
		finally {
			it.close();
		}
	}
	
	
	public NodeExpression getElse() {
		return else_;
	}

	
	@Override
	public List<String> getFunctionalSyntaxArguments() {
		return Arrays.asList(getInput().getFunctionalSyntax(), then != null ? then.getFunctionalSyntax() : "?none", else_ != null ? else_.getFunctionalSyntax() : "?none");
	}
	
	
	@Override
	protected String getFunctionalSyntaxName() {
		return "if";
	}


	@Override
	public List<NodeExpression> getInputExpressions() {
		List<NodeExpression> results = new LinkedList<>();
		results.add(getInput());
		results.add(getThen());
		if(getElse() != null) {
			results.add(getElse());
		}
		return results;
	}


	public NodeExpression getThen() {
		return then;
	}

	
	@Override
	public String getTypeId() {
		return "if";
	}
	
	
	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
