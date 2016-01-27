/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import java.util.Iterator;
import java.util.List;

import org.topbraid.spin.model.Element;
import org.topbraid.spin.model.Union;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.visitor.ElementVisitor;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;


public class UnionImpl extends ElementImpl implements Union {
	
	public UnionImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	
	
	public void print(PrintContext p) {
		List<Element> elements = getElements();
		for(Iterator<Element> it = elements.iterator(); it.hasNext(); ) {
			Element element = it.next();
			p.print("{");
			p.println();
			p.setIndentation(p.getIndentation() + 1);
			element.print(p);
			p.setIndentation(p.getIndentation() - 1);
			p.printIndentation(p.getIndentation());
			p.print("}");
			if(it.hasNext()) {
				p.println();
				p.printIndentation(p.getIndentation());
				p.printKeyword("UNION");
				p.println();
				p.printIndentation(p.getIndentation());
			}
		}
	}


	public void visit(ElementVisitor visitor) {
		visitor.visit(this);
	}
}
