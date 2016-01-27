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
 * Utility functions for ExpressionVisitors.
 * 
 * @author Holger Knublauch
 */
public class ExpressionVisitors {

	public static void visit(RDFNode node, ExpressionVisitor visitor) {
		if(node instanceof Variable) {
			visitor.visit((Variable)node);
		}
		else if(node instanceof FunctionCall) {
			visitor.visit((FunctionCall)node);
		}
		else if(node instanceof Aggregation) {
			visitor.visit((Aggregation)node);
		}
		else if(node != null) {
			visitor.visit(node);
		}
	}
}
