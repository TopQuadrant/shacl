package org.topbraid.shacl.expr.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.topbraid.shacl.expr.AbstractInputExpression;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionVisitor;

public class OrderByExpression extends AbstractInputExpression {
	
	private NodeExpression comparator;
	
	private boolean descending;
	

	public OrderByExpression(RDFNode expr, NodeExpression input, NodeExpression comparator, boolean descending) {
		super(expr, input);
		this.comparator = comparator;
		this.descending = descending;
	}

	
	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		List<RDFNode> list = new ArrayList<>(evalInput(focusNode, context).toList());
		Map<RDFNode,RDFNode> values = new HashMap<>();
		Collections.sort(list, new Comparator<RDFNode>() {
			@Override
			public int compare(RDFNode o1, RDFNode o2) {
				RDFNode v1 = getOrCompute(o1, values, context); 
				RDFNode v2 = getOrCompute(o2, values, context);
				if(v1 == null) {
					if(v2 == null) {
						return 0;
					}
					else {
						return descending ? 1 : -1;
					}
				}
				else if(v2 == null) {
					return descending ? -1 : 1;
				}
				else {
					int c = NodeValue.compareAlways(NodeValue.makeNode(v1.asNode()), NodeValue.makeNode(v2.asNode()));
					return descending ? -c : c;
				}
			}
		});
		return WrappedIterator.create(list.iterator());
	}
	
	
	private RDFNode getOrCompute(RDFNode key, Map<RDFNode,RDFNode> values, NodeExpressionContext context) {
		return values.computeIfAbsent(key, k -> {
			ExtendedIterator<RDFNode> it = comparator.eval(key, context);
			if(it.hasNext()) {
				RDFNode result = it.next();
				it.close();
				return result;
			}
			else {
				return null;
			}
		});
	}

	
	@Override
	public List<String> getFunctionalSyntaxArguments() {
		return Arrays.asList(getInput().getFunctionalSyntax(), comparator.getFunctionalSyntax(), descending ? "desc" : "asc");
	}
	
	
	@Override
	public List<NodeExpression> getInputExpressions() {
		return Arrays.asList(getInput(), comparator);
	}
	
	
	@Override
	public Resource getOutputShape(Resource contextShape) {
		return getInput().getOutputShape(contextShape);
	}


	@Override
	public String getTypeId() {
		return "orderBy";
	}
	
	
	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
