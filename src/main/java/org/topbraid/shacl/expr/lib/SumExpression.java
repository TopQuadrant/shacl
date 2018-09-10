package org.topbraid.shacl.expr.lib;

import java.util.Collections;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.XSDFuncOp;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.topbraid.shacl.expr.AbstractInputExpression;
import org.topbraid.shacl.expr.AppendContext;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionVisitor;
import org.topbraid.shacl.expr.SNEL;

/**
 * Implements support for sh:sum.
 * 
 * This node expression type is not part of the SHACL-AF 1.0 document, but a candidate for 1.1.
 * 
 * @author Holger Knublauch
 */
public class SumExpression extends AbstractInputExpression {
	
	public SumExpression(RDFNode expr, NodeExpression input) {
		super(expr, input);
	}

	
	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		ExtendedIterator<RDFNode> it = evalInput(focusNode, context);
		NodeValue total = NodeValue.nvZERO;
		while(it.hasNext()) {
			RDFNode n = it.next();
			NodeValue nv = NodeValue.makeNode(n.asNode());
			if (nv.isNumber()) {
				total = XSDFuncOp.numAdd(nv, total);
			}
			else {
				it.close();
				return WrappedIterator.emptyIterator();
			}
		}
		RDFNode result = focusNode.getModel().asRDFNode(total.asNode());
		List<RDFNode> results = Collections.singletonList(result);
		return WrappedIterator.create(results.iterator());
	}
	
	
	@Override
	public SNEL getTypeId() {
		return SNEL.sum;
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
