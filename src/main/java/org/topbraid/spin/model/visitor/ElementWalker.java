/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.visitor;

import java.util.List;

import org.topbraid.spin.model.Bind;
import org.topbraid.spin.model.Element;
import org.topbraid.spin.model.ElementGroup;
import org.topbraid.spin.model.ElementList;
import org.topbraid.spin.model.Exists;
import org.topbraid.spin.model.Filter;
import org.topbraid.spin.model.Minus;
import org.topbraid.spin.model.NamedGraph;
import org.topbraid.spin.model.NotExists;
import org.topbraid.spin.model.Optional;
import org.topbraid.spin.model.Service;
import org.topbraid.spin.model.SubQuery;
import org.topbraid.spin.model.TriplePath;
import org.topbraid.spin.model.TriplePattern;
import org.topbraid.spin.model.Union;
import org.topbraid.spin.model.Values;

import com.hp.hpl.jena.rdf.model.RDFNode;


/**
 * An object that can be used to recursively walk through an Element
 * and the embedded expressions.
 * 
 * @author Holger Knublauch
 */
public class ElementWalker implements ElementVisitor {
	
	private ElementVisitor elementVisitor;
	
	private ExpressionVisitor expressionVisitor;
	
	
	public ElementWalker(ElementVisitor elementVisitor, ExpressionVisitor expressionVisitor) {
		this.elementVisitor = elementVisitor;
		this.expressionVisitor = expressionVisitor;
	}


	public void visit(Bind bind) {
		elementVisitor.visit(bind);
		visitExpression(bind.getExpression());
	}

	
	public void visit(ElementList elementList) {
		elementVisitor.visit(elementList);
		visitChildren(elementList);
	}


	public void visit(Exists exists) {
		elementVisitor.visit(exists);
		visitChildren(exists);
	}


	public void visit(Filter filter) {
		elementVisitor.visit(filter);
		visitExpression(filter.getExpression());
	}


	@Override
	public void visit(Minus minus) {
		elementVisitor.visit(minus);
		visitChildren(minus);
	}


	public void visit(NamedGraph namedGraph) {
		elementVisitor.visit(namedGraph);
		visitChildren(namedGraph);
	}


	public void visit(NotExists notExists) {
		elementVisitor.visit(notExists);
		visitChildren(notExists);
	}


	public void visit(Optional optional) {
		elementVisitor.visit(optional);
		visitChildren(optional);
	}


	public void visit(Service service) {
		elementVisitor.visit(service);
		visitChildren(service);
	}


	public void visit(SubQuery subQuery) {
		elementVisitor.visit(subQuery);
	}


	public void visit(TriplePath triplePath) {
		elementVisitor.visit(triplePath);
	}


	public void visit(TriplePattern triplePattern) {
		elementVisitor.visit(triplePattern);
	}


	public void visit(Union union) {
		elementVisitor.visit(union);
		visitChildren(union);
	}


	public void visit(Values values) {
		elementVisitor.visit(values);
	}
	
	
	protected void visitChildren(ElementGroup group) {
		List<Element> childElements = group.getElements();
		for(Element childElement : childElements) {
			childElement.visit(this);
		}
	}
	
	
	private void visitExpression(RDFNode node) {
		if(expressionVisitor != null) {
			ExpressionWalker expressionWalker = new ExpressionWalker(expressionVisitor);
			ExpressionVisitors.visit(node, expressionWalker);
		}
	}
}
