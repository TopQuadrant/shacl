/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.Bind;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.visitor.ElementVisitor;
import org.topbraid.spin.vocabulary.SP;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;


public class BindImpl extends ElementImpl implements Bind {
    
	public BindImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	public RDFNode getExpression() {
		Statement s = getProperty(SP.expression);
		if(s != null) {
			return SPINFactory.asExpression(s.getObject());
		}
		else {
			return null;
		}
	}

	
	public Variable getVariable() {
		Statement s = getProperty(SP.variable);
		if(s != null && s.getObject().isResource()) {
			return s.getResource().as(Variable.class);
		}
		else {
			return null;
		}
	}
	
	
	public void print(PrintContext context) {
		context.printKeyword("BIND");
		context.print(" (");
		RDFNode expression = getExpression();
		if(expression != null) {
			printNestedExpressionString(context, expression);
		}
		else {
			context.print("<Error: Missing expression>");
		}
		context.print(" ");
		context.printKeyword("AS");
		context.print(" ");
		Variable variable = getVariable();
		if(variable != null) {
			context.print(variable.toString());
		}
		else {
			context.print("<Error: Missing variable>");
		}
		context.print(")");
	}


	public void visit(ElementVisitor visitor) {
		visitor.visit(this);
	}
}
