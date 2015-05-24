/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.Query;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.SubQuery;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.visitor.ElementVisitor;
import org.topbraid.spin.vocabulary.SP;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;



public class SubQueryImpl extends ElementImpl implements SubQuery {
    
	public SubQueryImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	public Query getQuery() {
		Resource r = getResource(SP.query);
		if(r != null) {
			return SPINFactory.asQuery(r);
		}
		else {
			return null;
		}
	}

	
	public void print(PrintContext p) {
		p.print("{");
		p.println();
		Query query = getQuery();
		if(query != null) {
			p.setIndentation(p.getIndentation() + 1);
			boolean oldPP = p.getPrintPrefixes();
			p.setPrintPrefixes(false);
			query.print(p);
			p.setIndentation(p.getIndentation() - 1);
			p.setPrintPrefixes(oldPP);
		}
		else {
			p.print("<Error: Missing sub-query>");
		}
		p.println();
		p.printIndentation(p.getIndentation());
		p.print("}");
	}

	
	public void visit(ElementVisitor visitor) {
		visitor.visit(this);
	}
}
