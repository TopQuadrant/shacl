/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.Template;
import org.topbraid.spin.vocabulary.SPIN;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;


public class TemplateImpl extends ModuleImpl implements Template {

	public TemplateImpl(Node node, EnhGraph eg) {
		super(node, eg);
	}

	
	public String getLabelTemplate() {
		return getString(SPIN.labelTemplate);
	}


	@Override
	public String getLabelTemplate(String matchLang) {
		
		if(matchLang == null || matchLang.equals("")) {
			return getLabelTemplate();
		}
		
		String label = null;
		for(Statement s : listProperties(SPIN.labelTemplate).toList()) {
			RDFNode object = s.getObject();
			if(object.isLiteral()) {
				Literal literal = (Literal)object;
				String lang = literal.getLanguage();
				if((lang.length() == 0 && label == null) || matchLang.equals(lang)) {
					label = literal.getString();
				}
			}
		}
		return label;
	}
}
