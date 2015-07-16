/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.visitor;

import org.topbraid.spin.model.Bind;
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


/**
 * Basic, "empty" implementation of ElementVisitor.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractElementVisitor implements ElementVisitor {


	public void visit(Bind let) {
	}

	
	public void visit(ElementList elementList) {
	}

	
	public void visit(Exists exists) {
	}


	public void visit(Filter filter) {
	}


	@Override
	public void visit(Minus minus) {
	}


	public void visit(NamedGraph namedGraph) {
	}
	
	
	public void visit(NotExists notExists) {
	}


	public void visit(Optional optional) {
	}


	public void visit(Service service) {
	}


	public void visit(SubQuery subQuery) {
	}


	public void visit(TriplePath triplePath) {
	}


	public void visit(TriplePattern triplePattern) {
	}


	public void visit(Union union) {
	}


	@Override
	public void visit(Values values) {
	}
}
