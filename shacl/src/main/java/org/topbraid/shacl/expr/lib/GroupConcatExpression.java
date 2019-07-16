package org.topbraid.shacl.expr.lib;

import java.util.Collections;
import java.util.List;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.topbraid.jenax.util.RDFLabels;
import org.topbraid.shacl.expr.AbstractInputExpression;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionVisitor;

/**
 * Implements support for sh:groupConcat.
 * 
 * This node expression type is not part of the SHACL-AF 1.0 document, but a candidate for 1.1.
 * 
 * @author Holger Knublauch
 */
public class GroupConcatExpression extends AbstractInputExpression {

	private String separator;
	
	
	public GroupConcatExpression(RDFNode expr, NodeExpression input, String separator) {
		super(expr, input);
		this.separator = separator;
	}

	
	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		StringBuffer sb = new StringBuffer();
		ExtendedIterator<RDFNode> it = evalInput(focusNode, context);
		while(it.hasNext()) {
			RDFNode node = it.next();
			if(node.isLiteral() && XSDDatatype.XSDstring.getURI().equals(node.asNode().getLiteralDatatypeURI())) {
				sb.append(node.asNode().getLiteralLexicalForm());
			}
			else {
				String label = RDFLabels.get().getNodeLabel(node);
				if(label != null) {
					sb.append(label);
				}
			}
			if(separator != null && it.hasNext()) {
				sb.append(separator);
			}
		}
		List<RDFNode> results = Collections.singletonList(ResourceFactory.createTypedLiteral(sb.toString()));
		return WrappedIterator.create(results.iterator());
	}
	
	
	@Override
	public String getTypeId() {
		return "groupConcat";
	}
	
	
	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
