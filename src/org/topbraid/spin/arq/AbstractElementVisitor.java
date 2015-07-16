/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.arq;

import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementAssign;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementDataset;
import com.hp.hpl.jena.sparql.syntax.ElementExists;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementMinus;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitor;


/**
 * A basic implementation of ElementVisitor that has handling of
 * ElementGroups so that they are recursively walked in.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractElementVisitor implements ElementVisitor {


	public void visit(ElementBind el) {
	}


	public void visit(ElementData el) {
	}


	public void visit(ElementExists arg0) {
	}


	public void visit(ElementNotExists arg0) {
	}


	public void visit(ElementAssign arg0) {
	}


	public void visit(ElementMinus el) {
	}


	public void visit(ElementSubQuery arg0) {
	}


	public void visit(ElementPathBlock arg0) {
	}


	public void visit(ElementTriplesBlock el) {
	}

	
	public void visit(ElementDataset dataset) {
	}
	
	
	public void visit(ElementFilter filter) {
	}

	
	public void visit(ElementGroup group) {
		for(Element element : group.getElements()) {
			element.visit(this);
		}
	}

	
	public void visit(ElementNamedGraph arg0) {
	}

	
	public void visit(ElementOptional arg0) {
	}

	
	public void visit(ElementService service) {
	}

	
	public void visit(ElementUnion arg0) {
	}
}
