/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.topbraid.spin.model.Element;
import org.topbraid.spin.model.ElementList;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.TriplePattern;
import org.topbraid.spin.model.Variable;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.print.StringPrintContext;
import org.topbraid.spin.model.visitor.ElementVisitor;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.RDFListImpl;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;


public class ElementListImpl extends RDFListImpl implements ElementList {

	public ElementListImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	
	
	private int addListMembers(List<Element> elements, int i, List<RDFNode> members) {
		boolean first = true;
		while(i < elements.size() - 1 && 
				elements.get(i) instanceof TriplePattern && 
				elements.get(i + 1) instanceof TriplePattern) {
			TriplePattern firstPattern = (TriplePattern) elements.get(i);
			TriplePattern secondPattern = (TriplePattern) elements.get(i + 1);
			if(RDF.first.equals(firstPattern.getPredicate()) && RDF.rest.equals(secondPattern.getPredicate())) {
				Resource firstSubject = firstPattern.getSubject();
				Resource secondSubject = secondPattern.getSubject();
				if(firstSubject instanceof Variable && secondSubject instanceof Variable) {
					Variable firstVar = (Variable) firstSubject;
					Variable secondVar = (Variable) secondSubject;
					if(firstVar.isBlankNodeVar() && firstVar.getName().equals(secondVar.getName())) {
						members.add(firstPattern.getObject());
						RDFNode secondObject = secondPattern.getObject();
						i++;
						if(RDF.nil.equals(secondObject)) {
							return i + 1;
						}
					}
				}
			}
			
			// We are not in a valid list
			if(first && members.isEmpty()) {
				break;
			}
			first = false;
			i++;
		}
		return i;
	}

	
	public List<Element> getElements() {
		List<Element> results = new LinkedList<Element>();
		ExtendedIterator<RDFNode> it = iterator();
		while(it.hasNext()) {
			RDFNode node = it.next();
			if(node.isResource()) {
				Element element = SPINFactory.asElement((Resource)node);
				if(element != null) {
					results.add(element);
				}
			}
		}
		return results;
	}
	
	
	private boolean nextIsMatchingVarPattern(TriplePattern main, List<Element> elements, int i) {
		if(main.getObject() instanceof Variable && 
				i < elements.size() - 2 &&
				elements.get(i + 1) instanceof TriplePattern &&
				elements.get(i + 2) instanceof TriplePattern) {
			Variable mainVar = (Variable) main.getObject();
			if(mainVar.isBlankNodeVar()) {
				TriplePattern nextPattern = (TriplePattern)elements.get(i + 1);
				TriplePattern lastPattern = (TriplePattern)elements.get(i + 2);
				Resource nextSubject = nextPattern.getSubject();
				Resource lastSubject = lastPattern.getSubject();
				if(nextSubject instanceof Variable && 
				   lastSubject instanceof Variable &&
						RDF.first.equals(nextPattern.getPredicate()) &&
						RDF.rest.equals(lastPattern.getPredicate())) {
					Variable nextVar = (Variable) nextSubject;
					if(mainVar.getName().equals(nextVar.getName())) {
						Variable lastVar = (Variable) lastSubject;
						return mainVar.getName().equals(lastVar.getName());
					}
				}
			}
		}
		return false;
	}
	
	
	public void print(PrintContext p) {
		List<Element> elements = getElements();
		
		int oldI = -1;
		for(int i = 0; i < elements.size(); i++) {
			if(i == oldI) {
				break; // Prevent unknown endless loop conditions
			}
			oldI = i;
			Element element = elements.get(i);
			p.printIndentation(p.getIndentation());
			if(element instanceof ElementList) {
				p.print("{");
				p.println();
				p.setIndentation(p.getIndentation() + 1);
				element.print(p);
				p.setIndentation(p.getIndentation() - 1);
				p.printIndentation(p.getIndentation());
				p.print("}");
			}
			else {
				if(element instanceof TriplePattern) {
					i = printTriplePattern(elements, i, p);
				}
				else {
					element.print(p);
				}
			}
			p.print(" .");
			p.println();
		}
	}
	

	// Special treatment of nested rdf:Lists
	private int printTriplePattern(List<Element> elements, int i, PrintContext p) {
		TriplePattern main = (TriplePattern) elements.get(i);
		
		// Print subject
		List<RDFNode> leftList = new ArrayList<RDFNode>();
		i = addListMembers(elements, i, leftList);
		if(leftList.isEmpty()) {
			TupleImpl.print(getModel(), main.getSubject(), p);
		}
		else {
			printRDFList(p, leftList);
			main = (TriplePattern) elements.get(i);
		}
		p.print(" ");
		
		// Print predicate
		if(RDF.type.equals(main.getPredicate())) {
			p.print("a");
		}
		else {
			TupleImpl.print(getModel(), main.getPredicate(), p);
		}
		p.print(" ");
		
		// Print object
		if(nextIsMatchingVarPattern(main, elements, i)) {
			List<RDFNode> rightList = new ArrayList<RDFNode>();
			i = addListMembers(elements, i + 1, rightList);
			if(rightList.isEmpty()) {
				TupleImpl.print(getModel(), main.getObject(), p);
				if(!leftList.isEmpty()) {
					i--;
				}
			}
			else {
				printRDFList(p, rightList);
				i--;
			}
		}
		else {
			TupleImpl.print(getModel(), main.getObject(), p);
		}
		return i;
	}
	
	
	private void printRDFList(PrintContext p, List<RDFNode> members) {
		p.print("(");
		for(RDFNode node : members) {
			p.print(" ");
			TupleImpl.print(getModel(), node, p);
		}
		p.print(" )");
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		PrintContext context = new StringPrintContext(sb);
		print(context);
		return sb.toString();
	}


	public void visit(ElementVisitor visitor) {
		visitor.visit(this);
	}
}
