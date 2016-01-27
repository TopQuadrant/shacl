/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.visitor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.topbraid.spin.model.Element;
import org.topbraid.spin.model.ElementList;
import org.topbraid.spin.model.FunctionCall;
import org.topbraid.spin.model.Query;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.TriplePattern;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.util.SPINUtil;
import org.topbraid.spin.vocabulary.SPIN;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;


/**
 * A utility that can be used to traverse all TriplePatterns under a given
 * root Element.  This also traverses function calls and simulates the
 * bindings of those function calls if a Function has a registered body. 
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractTriplesVisitor {
	
	// Needed to remember the bindings of a FunctionCall so that they can be substituted
	private Map<Property,RDFNode> bindings;
	
	private Element element;
	
	
	public AbstractTriplesVisitor(Element element, Map<Property,RDFNode> initialBindings) {
		this.bindings = initialBindings;
		this.element = element;
	}
	
	
	public void run() {
		ElementWalker walker = new ElementWalker(new MyElementVisitor(), new MyExpressionVisitor());
		element.visit(walker);
	}
	

	/**
	 * Will be called on each TriplePattern.
	 * @param triplePattern  the TriplePattern
	 */
	protected abstract void handleTriplePattern(TriplePattern triplePattern, Map<Property,RDFNode> bindings);

	
	// This visitor collects the relevant predicates
	private class MyElementVisitor extends AbstractElementVisitor {

		@Override
		public void visit(TriplePattern triplePattern) {
			handleTriplePattern(triplePattern, bindings);
		}
	};

	
	// This visitor walks into SPIN Function calls 
	private class MyExpressionVisitor extends AbstractExpressionVisitor {
		
		private Set<FunctionCall> reachedFunctionCalls = new HashSet<FunctionCall>();

		@Override
		public void visit(FunctionCall functionCall) {
			Resource function = functionCall.getFunction();
			if(function != null && function.isURIResource() && !reachedFunctionCalls.contains(functionCall)) {
				reachedFunctionCalls.add(functionCall);
				Resource f = SPINModuleRegistry.get().getFunction(function.getURI(), null);
				if(f != null) {
					Statement bodyS = f.getProperty(SPIN.body);
					if(bodyS != null && bodyS.getObject().isResource()) {
						
						Map<Property,RDFNode> oldBindings = bindings;
						bindings = functionCall.getArgumentsMap();
						if(oldBindings != null) {
							Map<String,RDFNode> varNamesBindings = SPINUtil.mapProperty2VarNames(oldBindings);
							SPINUtil.applyBindings(bindings, varNamesBindings);
						}
						
						Query spinQuery = SPINFactory.asQuery(bodyS.getResource());
						ElementList where = spinQuery.getWhere();
						if(where != null) {
							ElementWalker walker = new ElementWalker(new MyElementVisitor(), this);
							where.visit(walker);
						}
						
						bindings = oldBindings;
					}
				}
			}
		}
	};
}
