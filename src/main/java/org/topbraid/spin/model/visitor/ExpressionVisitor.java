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
 * A visitor to visit the various types of expression elements.
 * 
 * @author Holger Knublauch
 */
public interface ExpressionVisitor {
	
	void visit(Aggregation aggregation);
	
	
	void visit(FunctionCall functionCall);

	
	void visit(RDFNode node);
	
	
	void visit(Variable variable);
}
