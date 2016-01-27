/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.visitor;

import java.util.List;

import org.topbraid.spin.model.Aggregation;
import org.topbraid.spin.model.FunctionCall;
import org.topbraid.spin.model.Variable;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;


/**
 * An ExpressionVisitor that recursively visits all expressions under
 * a given root.
 * 
 * @author Holger Knublauch
 */
public class ExpressionWalker implements ExpressionVisitor {

	private ExpressionVisitor visitor;
	
	
	public ExpressionWalker(ExpressionVisitor visitor) {
		this.visitor = visitor;
	}

	
	public void visit(Aggregation aggregation) {
		visitor.visit(aggregation);
		Variable as = aggregation.getAs();
		if(as != null) {
			visitor.visit(as);
		}
		Resource expr = aggregation.getExpression();
		if(expr != null) {
			ExpressionVisitors.visit(expr, this);
		}
	}


	public void visit(FunctionCall functionCall) {
		visitor.visit(functionCall);
		List<RDFNode> args = functionCall.getArguments();
		for(RDFNode arg : args) {
			ExpressionVisitors.visit(arg, this);
		}
	}

	
	public void visit(RDFNode node) {
		visitor.visit(node);
	}

	
	public void visit(Variable variable) {
		visitor.visit(variable);
	}
}
