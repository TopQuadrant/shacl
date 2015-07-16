/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.update.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.model.Element;
import org.topbraid.spin.model.ElementList;
import org.topbraid.spin.model.NamedGraph;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.TripleTemplate;
import org.topbraid.spin.model.impl.AbstractSPINResourceImpl;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.update.Update;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SP;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public abstract class UpdateImpl extends AbstractSPINResourceImpl implements Update {

	public UpdateImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}


	public ElementList getWhere() {
		Statement whereS = getProperty(SP.where);
		if(whereS != null) {
			Element element = SPINFactory.asElement(whereS.getResource());
			return (ElementList) element;
		}
		else {
			return null;
		}
	}

	
	public boolean isSilent() {
		return hasProperty(SP.silent, JenaDatatypes.TRUE);
	}


	@Override
	public void print(PrintContext p) {
		String text = ARQ2SPIN.getTextOnly(this);
		if(text != null) {
			p.print(text);
		}
		else {
			printSPINRDF(p);
		}
	}
	
	
	protected abstract void printSPINRDF(PrintContext p);


	protected void printGraphDefaultNamedOrAll(PrintContext p) {
		Resource graph = JenaUtil.getResourceProperty(this, SP.graphIRI);
		if(graph != null) {
			p.printKeyword("GRAPH");
			p.print(" ");
			p.printURIResource(graph);
		}
		else if(hasProperty(SP.default_, JenaDatatypes.TRUE)) {
			p.printKeyword("DEFAULT");
		}
		else if(hasProperty(SP.named, JenaDatatypes.TRUE)) {
			p.printKeyword("NAMED");
		}
		else if(hasProperty(SP.all, JenaDatatypes.TRUE)) {
			p.printKeyword("ALL");
		}
	}


	protected void printGraphIRIs(PrintContext p, String keyword) {
		List<String> graphIRIs = new ArrayList<String>();
		{
			Iterator<Statement> it = listProperties(SP.graphIRI);
			while(it.hasNext()) {
				Statement s = it.next();
				if(s.getObject().isURIResource()) {
					graphIRIs.add(s.getResource().getURI());
				}
			}
			Collections.sort(graphIRIs);
		}
		for(String graphIRI : graphIRIs) {
			p.print(" ");
			if(keyword != null) {
				p.printKeyword(keyword);
				p.print(" ");
			}
			p.printURIResource(getModel().getResource(graphIRI));
		}
	}


	protected void printSilent(PrintContext p) {
		if(isSilent()) {
			p.printKeyword("SILENT");
			p.print(" ");
		}
	}
	
	
	protected boolean printTemplates(PrintContext p, Property predicate, String keyword, boolean force, Resource graphIRI) {
		List<RDFNode> nodes = getList(predicate);
		if(!nodes.isEmpty() || force) {
			if(keyword != null) {
				p.printIndentation(p.getIndentation());
				p.printKeyword(keyword);
			}
			p.print(" {");
			p.println();
			if(graphIRI != null) { // Legacy triple
				p.setIndentation(p.getIndentation() + 1);
				p.printIndentation(p.getIndentation());
				p.printKeyword("GRAPH");
				p.print(" ");
				printVarOrResource(p, graphIRI);
				p.print(" {");
				p.println();
			}
			for(RDFNode node : nodes) {
				p.printIndentation(p.getIndentation() + 1);
				if(node.canAs(NamedGraph.class)) {
					NamedGraph namedGraph = node.as(NamedGraph.class);
					p.setIndentation(p.getIndentation() + 1);
					p.setNamedBNodeMode(true);
					namedGraph.print(p);
					p.setNamedBNodeMode(false);
					p.setIndentation(p.getIndentation() - 1);
				}
				else {
					TripleTemplate template = node.as(TripleTemplate.class);
					template.print(p);
				}
				p.print(" .");
				p.println();
			}
			if(graphIRI != null) {
				p.printIndentation(p.getIndentation());
				p.setIndentation(p.getIndentation() - 1);
				p.print("}");
				p.println();
			}
			p.printIndentation(p.getIndentation());
			p.print("}");
			return true;
		}
		else {
			return false;
		}
	}


	protected void printWhere(PrintContext p) {
		p.printIndentation(p.getIndentation());
		p.printKeyword("WHERE");
		printNestedElementList(p, SP.where);
	}
}
