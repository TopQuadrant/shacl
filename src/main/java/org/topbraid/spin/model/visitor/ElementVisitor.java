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
 * An interface to visit the various kinds of Elements.
 * 
 * @author Holger Knublauch
 */
public interface ElementVisitor {
	
	void visit(Bind bind);
	
	
	void visit(ElementList elementList);
	
	
	void visit(Exists exists);
	
	
	void visit(Filter filter);
	
	
	void visit(Minus minus);
	
	
	void visit(NamedGraph namedGraph);
	
	
	void visit(NotExists notExists);
	
	
	void visit(Optional optional);

	
	void visit(Service service);
	
	
	void visit(SubQuery subQuery);
	
	
	void visit(TriplePath triplePath);

	
	void visit(TriplePattern triplePattern);
	
	
	void visit(Union union);
	
	
	void visit(Values values);
}
