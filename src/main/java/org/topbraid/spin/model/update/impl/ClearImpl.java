package org.topbraid.spin.model.update.impl;

import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.update.Clear;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;


public class ClearImpl extends UpdateImpl implements Clear {

	public ClearImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public void printSPINRDF(PrintContext p) {
		p.printKeyword("CLEAR");
		p.print(" ");
		printSilent(p);
		printGraphDefaultNamedOrAll(p);
	}
}
