/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.NamedGraph;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.visitor.ElementVisitor;
import org.topbraid.spin.vocabulary.SP;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;


public class NamedGraphImpl extends ElementImpl implements NamedGraph {
	
	public NamedGraphImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	
	
	public Resource getNameNode() {
		Resource r = getResource(SP.graphNameNode);
		if(r != null) {
			Variable variable = SPINFactory.asVariable(r);
			if(variable != null) {
				return variable;
			}
			else {
				return r;
			}
		}
		else {
			return null;
		}
	}


	public void print(PrintContext p) {
		p.printKeyword("GRAPH");
		p.print(" ");
		printVarOrResource(p, getNameNode());
		printNestedElementList(p);
	}


	public void visit(ElementVisitor visitor) {
		visitor.visit(this);
	}
}
