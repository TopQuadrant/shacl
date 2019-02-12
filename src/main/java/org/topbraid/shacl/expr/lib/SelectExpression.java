package org.topbraid.shacl.expr.lib;

import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.expr.AbstractSPARQLExpression;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionVisitor;

/**
 * Node expressions based on a SPARQL SELECT query, identified by sh:select.
 * 
 * This node expression type is not part of the SHACL-AF 1.0 document, but a candidate for 1.1.
 * 
 * @author Holger Knublauch
 */
public class SelectExpression extends AbstractSPARQLExpression {
	
	public SelectExpression(Resource expr, Query query, NodeExpression input, String queryString) {
		super(expr, query, input, queryString);
	}
	
	
	@Override
	public String getTypeId() {
		return "select";
	}
	
	
	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
