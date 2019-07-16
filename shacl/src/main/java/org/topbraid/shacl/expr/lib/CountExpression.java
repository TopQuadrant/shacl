package org.topbraid.shacl.expr.lib;

import java.util.Collections;
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
 * Implements support for sh:count.
 * 
 * This node expression type is not part of the SHACL-AF 1.0 document, but a candidate for 1.1.
 * 
 * @author Holger Knublauch
 */
public class CountExpression extends AbstractInputExpression {
	
	public CountExpression(RDFNode expr, NodeExpression input) {
		super(expr, input);
	}

	
	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		int count = 0;
		ExtendedIterator<RDFNode> it = evalInput(focusNode, context);
		while(it.hasNext()) {
			it.next();
			count++;
		}
		List<RDFNode> results = Collections.singletonList(JenaDatatypes.createInteger(count));
		return WrappedIterator.create(results.iterator());
	}
	
	
	@Override
	public String getTypeId() {
		return "count";
	}
	
	
	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
