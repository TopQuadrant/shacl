/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Service;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.visitor.ElementVisitor;
import org.topbraid.spin.vocabulary.SP;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Statement;

public class ServiceImpl extends ElementImpl implements Service {
	
	public ServiceImpl(Node node, EnhGraph enhGraph) {
		super(node, enhGraph);
	}

	
	public String getServiceURI() {
		Statement s = getProperty(SP.serviceURI);
		if(s != null && s.getObject().isURIResource()) {
			Variable variable = SPINFactory.asVariable(s.getResource());
			if(variable == null) {
				return s.getResource().getURI();
			}
		}
		return null;
	}


	@Override
	public Variable getServiceVariable() {
		Statement s = getProperty(SP.serviceURI);
		if(s != null && s.getObject().isResource()) {
			Variable variable = SPINFactory.asVariable(s.getResource());
			if(variable != null) {
				return variable;
			}
		}
		return null;
	}


	public void print(PrintContext p) {
		p.printKeyword("SERVICE");
		Variable var = getServiceVariable();
		if(var != null) {
			p.print(" ");
			p.printVariable(var.getName());
		}
		else {
			String uri = getServiceURI();
			if(uri != null) {
				p.print(" ");
				p.printURIResource(getModel().getResource(uri));
			}
		}
		printNestedElementList(p);
	}


	public void visit(ElementVisitor visitor) {
		visitor.visit(this);
	}
}
