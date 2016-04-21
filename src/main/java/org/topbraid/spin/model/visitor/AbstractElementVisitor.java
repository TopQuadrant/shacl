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


	@Override
    public void visit(Bind let) {
	}

	
	@Override
    public void visit(ElementList elementList) {
	}

	
	@Override
    public void visit(Exists exists) {
	}


	@Override
    public void visit(Filter filter) {
	}


	@Override
	public void visit(Minus minus) {
	}


	@Override
    public void visit(NamedGraph namedGraph) {
	}
	
	
	@Override
    public void visit(NotExists notExists) {
	}


	@Override
    public void visit(Optional optional) {
	}


	@Override
    public void visit(Service service) {
	}


	@Override
    public void visit(SubQuery subQuery) {
	}


	@Override
    public void visit(TriplePath triplePath) {
	}


	@Override
    public void visit(TriplePattern triplePattern) {
	}


	@Override
    public void visit(Union union) {
	}


	@Override
	public void visit(Values values) {
	}
}
