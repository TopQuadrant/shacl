package org.topbraid.spin.model.update.impl;

import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.update.Create;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SP;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;


public class CreateImpl extends UpdateImpl implements Create {

	public CreateImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public void printSPINRDF(PrintContext p) {
		p.printKeyword("CREATE");
		p.print(" ");
		printSilent(p);
		p.printKeyword("GRAPH");
		p.print(" ");
		p.printURIResource(JenaUtil.getResourceProperty(this, SP.graphIRI));
	}
}
