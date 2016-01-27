package org.topbraid.spin.model.update.impl;

import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.update.DeleteWhere;
import org.topbraid.spin.vocabulary.SP;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;


public class DeleteWhereImpl extends UpdateImpl implements DeleteWhere {

	public DeleteWhereImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public void printSPINRDF(PrintContext p) {
		printComment(p);
		printPrefixes(p);
		p.printIndentation(p.getIndentation());
		p.printKeyword("DELETE");
		p.print(" ");
		p.printKeyword("WHERE");
		printNestedElementList(p, SP.where);
	}
}
