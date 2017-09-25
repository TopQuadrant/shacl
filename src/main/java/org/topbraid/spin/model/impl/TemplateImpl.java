/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

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

	
	@Override
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
