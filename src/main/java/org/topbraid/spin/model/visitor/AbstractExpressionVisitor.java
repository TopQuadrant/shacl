/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.visitor;

import org.topbraid.spin.model.Aggregation;
import org.topbraid.spin.model.FunctionCall;
import org.topbraid.spin.model.Variable;

import org.apache.jena.rdf.model.RDFNode;


/**
 * An "empty" base implementation of ExpressionVisitor.
 * 
 * @author Holger Knublauch
 */
public class AbstractExpressionVisitor implements ExpressionVisitor {

	@Override
    public void visit(Aggregation aggregation) {
	}

	
	@Override
    public void visit(FunctionCall functionCall) {
	}


	@Override
    public void visit(RDFNode node) {
	}


	@Override
    public void visit(Variable variable) {
	}
}
