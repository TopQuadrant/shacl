package org.topbraid.shacl.expr.lib;

import java.util.Collections;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.shacl.expr.AbstractInputExpression;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionVisitor;

/**
 * Implements support for sh:exists.
 * 
 * This node expression type is not part of the SHACL-AF 1.0 document, but a candidate for 1.1.
 * 
 * @author Holger Knublauch
 */
public class ExistsExpression extends AbstractInputExpression {
	
	public ExistsExpression(RDFNode expr, NodeExpression input) {
		super(expr, input);
	}

	
	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		ExtendedIterator<RDFNode> it = evalInput(focusNode, context);
		RDFNode result = it.hasNext() ? JenaDatatypes.TRUE : JenaDatatypes.FALSE;
		it.close();
		return WrappedIterator.create(Collections.singletonList(result).iterator());
	}
	
	
	@Override
	public String getTypeId() {
		return "exists";
	}
	
	
	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
