package org.topbraid.shacl.expr.lib;

import java.util.Collections;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.topbraid.shacl.expr.AbstractInputExpression;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionVisitor;

/**
 * Implements support for sh:min.
 * 
 * This node expression type is not part of the SHACL-AF 1.0 document, but a candidate for 1.1.
 * 
 * @author Holger Knublauch
 */
public class MinExpression extends AbstractInputExpression {
	
	public MinExpression(RDFNode expr, NodeExpression input) {
		super(expr, input);
	}

	
	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		RDFNode min = null;
		ExtendedIterator<RDFNode> it = evalInput(focusNode, context);
		while(it.hasNext()) {
			RDFNode next = it.next();
			if(min == null || NodeValue.compareAlways(NodeValue.makeNode(min.asNode()), NodeValue.makeNode(next.asNode())) > 0) {
				min = next;
			}
		}
		if(min == null) {
			return WrappedIterator.emptyIterator();
		}
		else {
			return WrappedIterator.create(Collections.singletonList(min).iterator());
		}
	}
	
	
	@Override
	public String getTypeId() {
		return "min";
	}
	
	
	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
