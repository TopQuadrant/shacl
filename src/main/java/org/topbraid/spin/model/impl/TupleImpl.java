/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.util.SPINExpressions;
import org.topbraid.spin.vocabulary.SP;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.vocabulary.RDF;


abstract class TupleImpl extends AbstractSPINResourceImpl {

	public TupleImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	
	
	public RDFNode getObject() {
		return getRDFNodeOrVariable(SP.object);
	}

	
	public Resource getObjectResource() {
		RDFNode node = getRDFNodeOrVariable(SP.object);
		if(node instanceof Resource) {
			return (Resource) node;
		}
		else {
			return null;
		}
	}
	
	
	public Resource getSubject() {
		return (Resource) getRDFNodeOrVariable(SP.subject);
	}
	
	
	protected RDFNode getRDFNodeOrVariable(Property predicate) {
		RDFNode node = getRDFNode(predicate);
		if(node != null) {
			Variable var = SPINFactory.asVariable(node);
			if(var != null) {
				return var;
			}
			else {
				return node;
			}
		}
		else {
			return null;
		}
	}


	protected void print(RDFNode node, PrintContext p) {
		TupleImpl.print(getModel(), node, p);
	}


	protected void print(RDFNode node, PrintContext p, boolean abbrevRDFType) {
		TupleImpl.print(getModel(), node, p, abbrevRDFType);
	}


	public static void print(Model model, RDFNode node, PrintContext p) {
		print(model, node, p, false);
	}
	

	public static void print(Model model, RDFNode node, PrintContext p, boolean abbrevRDFType) {
		if(node instanceof Resource) {
			if(abbrevRDFType && RDF.type.equals(node)) {
				p.print("a");
			}
			else {
				Resource resource = (Resource)node;
				printVarOrResource(p, resource);
			}
		}
		else {
			PrefixMapping pm = p.getUsePrefixes() ? model.getGraph().getPrefixMapping() : SPINExpressions.emptyPrefixMapping;
			String str = FmtUtils.stringForNode(node.asNode(), pm);
			p.print(str);
		}
	}
}
