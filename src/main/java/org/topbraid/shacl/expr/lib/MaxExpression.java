package org.topbraid.shacl.expr.lib;

import java.util.Collections;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.topbraid.shacl.expr.AbstractInputExpression;
import org.topbraid.shacl.expr.AppendContext;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionVisitor;
import org.topbraid.shacl.expr.SNEL;

/**
 * Implements support for sh:max.
 * 
 * This node expression type is not part of the SHACL-AF 1.0 document, but a candidate for 1.1.
 * 
 * @author Holger Knublauch
 */
public class MaxExpression extends AbstractInputExpression {
	
	public MaxExpression(RDFNode expr, NodeExpression input) {
		super(expr, input);
	}

	
	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		RDFNode max = null;
		ExtendedIterator<RDFNode> it = evalInput(focusNode, context);
		while(it.hasNext()) {
			RDFNode next = it.next();
			if(max == null || NodeValue.compareAlways(NodeValue.makeNode(max.asNode()), NodeValue.makeNode(next.asNode())) < 0) {
				max = next;
			}
		}
		if(max == null) {
			return WrappedIterator.emptyIterator();
		}
		else {
			return WrappedIterator.create(Collections.singletonList(max).iterator());
		}
	}
	
	
	@Override
	public SNEL getTypeId() {
		return SNEL.max;
	}


	@Override
	public void appendSPARQL(AppendContext context, String targetVarName) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	
	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
