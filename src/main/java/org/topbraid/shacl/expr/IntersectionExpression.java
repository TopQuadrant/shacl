package org.topbraid.shacl.expr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;

public class IntersectionExpression implements NodeExpression {
	
	private List<NodeExpression> inputs;
	
	
	public IntersectionExpression(List<NodeExpression> inputs) {
		this.inputs = inputs;
	}

	
	@Override
	public List<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		Iterator<NodeExpression> it = inputs.iterator();
		Set<RDFNode> results = new HashSet<RDFNode>(it.next().eval(focusNode, context));
		while(it.hasNext()) {
			results.retainAll(it.next().eval(focusNode, context));
		}
		return new ArrayList<RDFNode>(results);
	}
}
