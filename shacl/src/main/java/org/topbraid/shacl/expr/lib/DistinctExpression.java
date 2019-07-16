package org.topbraid.shacl.expr.lib;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NiceIterator;
import org.topbraid.shacl.expr.AbstractInputExpression;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionVisitor;

/**
 * Implements support for sh:distinct.
 * 
 * This node expression type is not part of the SHACL-AF 1.0 document, but a candidate for 1.1.
 * 
 * @author Holger Knublauch
 */
public class DistinctExpression extends AbstractInputExpression {
	
	
	public DistinctExpression(RDFNode expr, NodeExpression input) {
		super(expr, input);
	}
	

	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
    	return distinct(evalInput(focusNode, context));
	}
	
	
	@Override
	public Resource getOutputShape(Resource contextShape) {
		return getInput().getOutputShape(contextShape);
	}


	@Override
	public String getTypeId() {
		return "distinct";
	}

	
	/**
	 * Produces an RDFNode iterator that drops duplicate values, based on a Set.
	 * @param base  the iterator to wrap
	 * @return a distinct iterator
	 */
	public static ExtendedIterator<RDFNode> distinct(ExtendedIterator<RDFNode> base) {
		Set<RDFNode> seen = new HashSet<>();
        return recording(rejecting(base, seen), seen);
	}

	
	private static <T> ExtendedIterator<T> recording(ClosableIterator<T> i, Set<T> seen) {
		return new NiceIterator<T>() {
			
			@Override
			public void remove() {
				i.remove();
			}

			@Override
			public boolean hasNext() {
				return i.hasNext(); 
			}    

			@Override
			public T next() { 
				T x = i.next(); 
				seen.add(x);
				return x;
			}  

			@Override
			public void close() {
				i.close(); 
			}
		};
	}

	
	private static ExtendedIterator<RDFNode> rejecting(ExtendedIterator<RDFNode> i, Set<RDFNode> seen) {
		return i.filterDrop(seen::contains);
	}
	
	
	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
