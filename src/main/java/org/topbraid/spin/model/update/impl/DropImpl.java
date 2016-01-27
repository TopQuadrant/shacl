package org.topbraid.spin.model.update.impl;

import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.update.Drop;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;


public class DropImpl extends UpdateImpl implements Drop {

	public DropImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public void printSPINRDF(PrintContext p) {
		p.printKeyword("DROP");
		p.print(" ");
		printSilent(p);
		printGraphDefaultNamedOrAll(p);
	}
}
