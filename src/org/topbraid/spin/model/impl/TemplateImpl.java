/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.Template;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;


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
