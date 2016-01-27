package org.topbraid.spin.model.update.impl;

import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.update.Load;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SP;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;


public class LoadImpl extends UpdateImpl implements Load {

	public LoadImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public void printSPINRDF(PrintContext p) {
		p.printKeyword("LOAD");
		p.print(" ");
		printSilent(p);
		Resource document = JenaUtil.getResourceProperty(this, SP.document);
		p.printURIResource(document);
		Resource into = JenaUtil.getResourceProperty(this, SP.into);
		if(into != null) {
			p.print(" ");
			p.printKeyword("INTO");
			p.print(" ");
			p.printKeyword("GRAPH");
			p.print(" ");
			p.printURIResource(into);
		}
	}
}
