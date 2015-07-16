package org.topbraid.spin.model.update.impl;

import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.update.DeleteData;
import org.topbraid.spin.vocabulary.SP;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;


public class DeleteDataImpl extends UpdateImpl implements DeleteData {

	public DeleteDataImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public void printSPINRDF(PrintContext p) {
		p.printKeyword("DELETE");
		p.print(" ");
		p.printKeyword("DATA");
		printTemplates(p, SP.data, null, true, null);
	}
}
