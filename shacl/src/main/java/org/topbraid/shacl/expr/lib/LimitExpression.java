package org.topbraid.shacl.expr.lib;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.topbraid.shacl.expr.AbstractInputExpression;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionVisitor;

/**
 * Implements support for sh:limit.
 * 
 * This node expression type is not part of the SHACL-AF 1.0 document, but a candidate for 1.1.
 * 
 * @author Holger Knublauch
 */
public class LimitExpression extends AbstractInputExpression {
	
	private int limit;

	
	public LimitExpression(RDFNode expr, NodeExpression input, int limit) {
		super(expr, input);
		this.limit = limit;
	}

	
	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		ExtendedIterator<RDFNode> it = evalInput(focusNode, context);
		return new WrappedIterator<RDFNode>(it) {
			
			private int count = 0;
			
			@Override
			public boolean hasNext() {
				if(count >= limit) {
					close();
					return false;
				}
				else {
					return it.hasNext();
				}
			}

			@Override
			public RDFNode next() {
				RDFNode n = super.next();
				count++;
				return n;
			}
		};
	}

	
	@Override
	public List<String> getFunctionalSyntaxArguments() {
		return Arrays.asList(getInput().getFunctionalSyntax(), "" + limit);
	}
	
	
	public int getLimit() {
		return limit;
	}
	
	
	@Override
	public Resource getOutputShape(Resource contextShape) {
		return getInput().getOutputShape(contextShape);
	}

	
	@Override
	public String getTypeId() {
		return "limit";
	}
	
	
	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
