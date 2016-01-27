/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.update.impl;

import java.util.List;

import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.update.Modify;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SP;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

public class ModifyImpl extends UpdateImpl implements Modify {

	public ModifyImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	
	
	public List<Resource> getUsing() {
		return JenaUtil.getResourceProperties(this, SP.using);
	}
	
	
	public List<Resource> getUsingNamed() {
		return JenaUtil.getResourceProperties(this, SP.usingNamed);
	}

	
	public void printSPINRDF(PrintContext p) {
		printComment(p);
		printPrefixes(p);
		
		Resource iri = JenaUtil.getResourceProperty(this, SP.graphIRI);
		
		Resource with = JenaUtil.getResourceProperty(this, SP.with);
		if(with != null) {
			p.printIndentation(p.getIndentation());
			p.printKeyword("WITH");
			p.print(" ");
			p.printURIResource(with);
			p.println();
		}

		if(printTemplates(p, SP.deletePattern, "DELETE", hasProperty(SP.deletePattern), iri)) {
			p.print("\n");
		}
		if(printTemplates(p, SP.insertPattern, "INSERT", hasProperty(SP.insertPattern), iri)) {
			p.print("\n");
		}

		for(Resource using : getUsing()) {
			p.printKeyword("USING");
			p.print(" ");
			p.printURIResource(using);
			p.println();
		}

		for(Resource usingNamed : getUsingNamed()) {
			p.printKeyword("USING");
			p.print(" ");
			p.printKeyword("NAMED");
			p.print(" ");
			p.printURIResource(usingNamed);
			p.println();
		}
		
		printWhere(p);
	}
}
