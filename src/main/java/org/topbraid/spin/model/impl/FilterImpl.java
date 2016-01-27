/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.Filter;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.visitor.ElementVisitor;
import org.topbraid.spin.vocabulary.SP;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;


public class FilterImpl extends ElementImpl implements Filter {
	
	public FilterImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	public RDFNode getExpression() {
		Statement s = getProperty(SP.expression);
		if(s != null) {
			RDFNode object = s.getObject();
			return SPINFactory.asExpression(object);
		}
		else {
			return null;
		}
	}
	
	
	public void print(PrintContext context) {
		context.printKeyword("FILTER");
		context.print(" ");
		RDFNode expression = getExpression();
		if(expression == null) {
			context.print("<Error: Missing expression>");
		}
		else {
			printNestedExpressionString(context, expression, true);
		}
	}


	public void visit(ElementVisitor visitor) {
		visitor.visit(this);
	}
}
