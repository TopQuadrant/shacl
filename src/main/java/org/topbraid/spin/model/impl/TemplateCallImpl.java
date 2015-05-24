/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import java.util.HashMap;
import java.util.Map;

import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Argument;
import org.topbraid.spin.model.Module;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Template;
import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.model.print.StringPrintContext;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;


public class TemplateCallImpl extends ModuleCallImpl implements TemplateCall {

	public TemplateCallImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public QueryExecution createQueryExecution(Dataset dataset) {
		Module template = getModule();
		Query query = ARQFactory.get().createQuery(SPINFactory.asQuery(template.getBody()));
		QuerySolutionMap initialBindings = new QuerySolutionMap();
		Map<Argument,RDFNode> args = getArgumentsMap();
		for(Argument arg : args.keySet()) {
			RDFNode value = args.get(arg);
			initialBindings.add(arg.getVarName(), value);
		}
		return ARQFactory.get().createQueryExecution(query, dataset, initialBindings);
	}


	public Map<Argument,RDFNode> getArgumentsMap() {
		Map<Argument,RDFNode> map = new HashMap<Argument,RDFNode>();
		Template template = getTemplate();
		if(template != null) {
			for(Argument ad : template.getArguments(false)) {
				Property argProperty = ad.getPredicate();
				if(argProperty != null) {
					Statement valueS = getProperty(argProperty);
					if(valueS != null) {
						map.put(ad, valueS.getObject());
					}
				}
			}
		}
		
		return map;
	}


	public Map<Property, RDFNode> getArgumentsMapByProperties() {
		Map<Property,RDFNode> map = new HashMap<Property,RDFNode>();
		Template template = getTemplate();
		if(template != null) {
			for(Argument ad : template.getArguments(false)) {
				Property argProperty = ad.getPredicate();
				if(argProperty != null) {
					Statement valueS = getProperty(argProperty);
					if(valueS != null) {
						map.put(argProperty, valueS.getObject());
					}
				}
			}
		}
		
		return map;
	}


	public Map<String, RDFNode> getArgumentsMapByVarNames() {
		Map<String,RDFNode> map = new HashMap<String,RDFNode>();
		Template template = getTemplate();
		if(template != null) {
			for(Argument ad : template.getArguments(false)) {
				Property argProperty = ad.getPredicate();
				if(argProperty != null) {
					String varName = ad.getVarName();
					Statement valueS = getProperty(argProperty);
					if(valueS != null) {
						map.put(varName, valueS.getObject());
					}
					else if(ad.getDefaultValue() != null) {
						map.put(varName, ad.getDefaultValue());
					}
				}
			}
		}
		return map;
	}

	
	@Override
	public QuerySolutionMap getInitialBinding() {
		QuerySolutionMap map = new QuerySolutionMap();
		Map<String,RDFNode> input = getArgumentsMapByVarNames();
		for(String varName : input.keySet()) {
			RDFNode value = input.get(varName);
			map.add(varName, value);
		}
		return map;
	}


	@Override
	public Module getModule() {
		return getTemplate();
	}


	public String getQueryString() {
		Map<String,RDFNode> map = getArgumentsMapByVarNames();
		StringPrintContext p = new StringPrintContext(new StringBuilder(), map);
		Template template = getTemplate();
		p.setUsePrefixes(false);
		template.getBody().print(p);
		return p.getString();
	}


	public Template getTemplate() {
		Statement s = getProperty(RDF.type);
		if(s != null && s.getObject().isURIResource()) {
			return SPINModuleRegistry.get().getTemplate(s.getResource().getURI(), getModel());
		}
		else {
			return null;
		}
	}


	public void print(PrintContext p) {
		Template template = getTemplate();
		String str = template.getLabelTemplate();
		if(str != null) {
			Map<String,RDFNode> args = getArgumentsMapByVarNames();
			StringBuffer buffer = new StringBuffer();
			SPINLabels.appendTemplateCallLabel(buffer, str, args);
			p.print(buffer.toString());
		}
		else if(template.getComment() != null) {
			p.print(template.getComment());
		}
		else {
			p.print("<No " + SPIN.PREFIX + ":" + SPIN.labelTemplate.getLocalName() + " set for " + template.getURI() + ">");
		}
	}
}
