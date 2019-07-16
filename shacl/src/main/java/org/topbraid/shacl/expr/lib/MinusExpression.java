package org.topbraid.shacl.expr.lib;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.topbraid.shacl.expr.AbstractInputExpression;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionVisitor;

/**
 * A sh:minus expression.
 * 
 * This node expression type is not part of the SHACL-AF 1.0 document, but a candidate for 1.1.
 * 
 * @author Holger Knublauch
 */
public class MinusExpression extends AbstractInputExpression {
	
	private NodeExpression minus;
	
	
	public MinusExpression(RDFNode expr, NodeExpression nodes, NodeExpression minus) {
		super(expr, nodes);
		this.minus = minus;
	}

	
	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		Set<RDFNode> sans = minus.eval(focusNode, context).toSet();
		return getInput().eval(focusNode, context).filterDrop(n -> sans.contains(n));
	}


	@Override
	public List<String> getFunctionalSyntaxArguments() {
		List<String> results = new LinkedList<>();
		results.add(getInput().getFunctionalSyntax());
		results.add(minus.getFunctionalSyntax());
		return results;
	}
	
	
	@Override
	public List<NodeExpression> getInputExpressions() {
		return Arrays.asList(getInput(), minus);
	}
	
	
	@Override
	public Resource getOutputShape(Resource contextShape) {
		return getInput().getOutputShape(contextShape);
	}


	@Override
	public String getTypeId() {
		return "minus";
	}
	
	
	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
