/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import java.util.LinkedList;
import java.util.List;

import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.model.Element;
import org.topbraid.spin.model.ElementList;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.SolutionModifierQuery;
import org.topbraid.spin.model.Values;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.print.Printable;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SP;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;


public abstract class QueryImpl extends AbstractSPINResourceImpl implements SolutionModifierQuery {
	
	
	public QueryImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}


	public List<String> getFrom() {
		return getStringList(SP.from);
	}


	public List<String> getFromNamed() {
		return getStringList(SP.fromNamed);
	}
	
	
	public Long getLimit() {
		return getLong(SP.limit);
	}


	public Long getOffset() {
		return getLong(SP.offset);
	}


	private List<String> getStringList(Property predicate) {
		List<String> results = new LinkedList<String>();
		StmtIterator it = listProperties(predicate);
		while(it.hasNext()) {
			RDFNode node = it.nextStatement().getObject();
			if(node.isLiteral()) {
				results.add(((Literal)node).getLexicalForm());
			}
			else if(node.isURIResource()) {
				results.add(((Resource)node).getURI());
			}
		}
		return results;
	}
	
	
	@Override
	public Values getValues() {
		Resource values = JenaUtil.getResourceProperty(this, SP.values);
		if(values != null) {
			return values.as(Values.class);
		}
		else {
			return null;
		}
	}


	public ElementList getWhere() {
		Statement whereS = getProperty(SP.where);
		if(whereS != null) {
			Element element = SPINFactory.asElement(whereS.getResource());
			return (ElementList) element;
		}
		else {
			return null;
		}
	}


	public List<Element> getWhereElements() {
		return getElements(SP.where);
	}


	@Override
	public void print(PrintContext p) {
		String text = ARQ2SPIN.getTextOnly(this);
		if(text != null) {
			if(p.hasInitialBindings()) {
				throw new IllegalArgumentException("Queries that only have an sp:text cannot be converted to a query string if initial bindings are present.");
			}
			else {
				p.print(text);
			}
		}
		else {
			printSPINRDF(p);
		}
	}
	
	
	protected abstract void printSPINRDF(PrintContext p);


	protected void printStringFrom(PrintContext context) {
		for(String from : getFrom()) {
			context.println();
			context.printKeyword("FROM");
			context.print(" <");
			context.print(from);
			context.print(">");
		}
		for(String fromNamed : getFromNamed()) {
			context.println();
			context.printKeyword("FROM NAMED");
			context.print(" <");
			context.print(fromNamed);
			context.print(">");
		}
	}
	
	
	protected void printSolutionModifiers(PrintContext context) {
		List<RDFNode> orderBy = getList(SP.orderBy);
		if(!orderBy.isEmpty()) {
			context.println();
			context.printIndentation(context.getIndentation());
			context.printKeyword("ORDER BY");
			for(RDFNode node : orderBy) {
				if(node.isResource()) {
					Resource resource = (Resource) node;
					if(resource.hasProperty(RDF.type, SP.Asc)) {
						context.print(" ");
						context.printKeyword("ASC");
						context.print(" ");
						RDFNode expression = resource.getProperty(SP.expression).getObject();
						printOrderByExpression(context, expression);
					}
					else if(resource.hasProperty(RDF.type, SP.Desc)) {
						context.print(" ");
						context.printKeyword("DESC");
						context.print(" ");
						RDFNode expression = resource.getProperty(SP.expression).getObject();
						printOrderByExpression(context, expression);
					}
					else {
						context.print(" ");
						printOrderByExpression(context, node);
					}
				}
			}
		}
		Long limit = getLimit();
		if(limit != null) {
			context.println();
			context.printIndentation(context.getIndentation());
			context.printKeyword("LIMIT");
			context.print(" " + limit);
		}
		Long offset = getOffset();
		if(offset != null) {
			context.println();
			context.printIndentation(context.getIndentation());
			context.print("OFFSET");
			context.print(" " + offset);
		}
	}


	private void printOrderByExpression(PrintContext sb, RDFNode node) {
		
		if(node instanceof Resource) {
			Resource resource = (Resource) node;
			Printable printable = SPINFactory.asAggregation(resource);
			if(printable == null) {
				printable = SPINFactory.asFunctionCall(resource);
			}
			if(printable != null) {
				sb.print("(");
				PrintContext pc = sb.clone();
				pc.setNested(true);
				printable.print(pc);
				sb.print(")");
				return;
			}
		}
		
		printNestedExpressionString(sb, node, true);
	}


	protected void printValues(PrintContext p) {
		Values values = getValues();
		if(values != null) {
			p.println();
			values.print(p);
		}
	}


	protected void printWhere(PrintContext p) {
		p.printIndentation(p.getIndentation());
		p.printKeyword("WHERE");
		printNestedElementList(p, SP.where);
	}
}
