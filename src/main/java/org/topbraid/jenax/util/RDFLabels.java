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

package org.topbraid.jenax.util;

import java.util.Iterator;
import java.util.Map;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;


/**
 * A singleton that is used to render resources into strings.
 * By default, this displays qnames (if possible).
 * Can be changed, for example, to switch to displaying rdfs:labels
 * instead of qnames etc.
 * 
 * @author Holger Knublauch
 */
public class RDFLabels {
	
	private static RDFLabels singleton = new RDFLabels();
	

	/**
	 * Gets the singleton instance of this class.
	 * @return the singleton
	 */
	public static RDFLabels get() {
		return singleton;
	}
	
	
	/**
	 * Replaces the singleton to a subclass with different behavior.
	 * This is used by TopBraid, which has its own rendering engine. 
	 * @param value  the new engine
	 */
	public static void set(RDFLabels value) {
		RDFLabels.singleton = value;
	}
	

	/**
	 * Gets a "human-readable" label for a blank node,
	 * e.g. Manchester Syntax for OWL restrictions.
	 * The default implementation doesn't do much yet it's overloaded in TopBraid.
	 * @param resource  the blank node resource to get the label for
	 * @return a label for the bnode
	 */
	public String getBlankNodeLabel(Resource resource) {
		return "_:" + resource.getId();
	}
	
	
	/**
	 * Gets a "human-readable" label for a given Resource.
	 * This checks for any existing rdfs:label, otherwise falls back to
	 * <code>getLabel()</code>.
	 * @param resource  the resource to get the label for
	 * @return the label (never null)
	 */
	public String getCustomizedLabel(Resource resource) {
		String label = JenaUtil.getStringProperty(resource, RDFS.label);
		if(label != null) {
			return label;
		}
		else {
			return getLabel(resource);
		}
	}
	

	/**
	 * Gets the label for a given Resource.
	 * @param resource  the Resource to get the label of
	 * @return the label (never null)
	 */
	public String getLabel(Resource resource) {
		if(resource.isURIResource() && resource.getModel() != null) {
			String qname = resource.getModel().qnameFor(resource.getURI());
			if(qname != null) {
				return qname;
			}
			else {
				return "<" + resource.getURI() + ">";
			}
		}
		else if(resource.isAnon() && resource.getModel() != null && resource.hasProperty(RDF.first)) {
			StringBuffer buffer = new StringBuffer("[");
			Iterator<RDFNode> members = resource.as(RDFList.class).iterator();
			while(members.hasNext()) {
				RDFNode member = members.next();
				buffer.append(RDFLabels.get().getNodeLabel(member));
				if(members.hasNext()) {
					buffer.append(", ");
				}
			}
			buffer.append("]");
			return buffer.toString();
		}
		else if(resource.isAnon()) {
			return getBlankNodeLabel(resource);
		}
		else {
			return resource.toString();
		}
	}
	
	
	public String getNodeLabel(RDFNode node) {
		if(node.isLiteral()) {
			return node.asNode().getLiteralLexicalForm();
		}
		else {
			return getLabel((Resource)node);
		}
	}


	/**
	 * Renders a template call's label template into a label by inserting the
	 * evaluated SPARQL expressions into appropriate spaces marked with {expression}.
	 * Currently only simple variables are supported, e.g. {?test }. 
	 * @param buffer  the StringBuffer to write to
	 * @param labelTemplate  the labelTemplate
	 * @param args  the arguments  a Map of pre-bound variables (supplied arguments)
	 */
	public static void appendTemplateCallLabel(StringBuffer buffer, String labelTemplate, Map<String, RDFNode> args) {
		for(int i = 0; i < labelTemplate.length(); i++) {
			if(i < labelTemplate.length() - 3 && labelTemplate.charAt(i) == '{' && labelTemplate.charAt(i + 1) == '?') {
				int varEnd = i + 2;
				while(varEnd < labelTemplate.length()) {
					if(labelTemplate.charAt(varEnd) == '}') {
						String varName = labelTemplate.substring(i + 2, varEnd);
						RDFNode varValue = args.get(varName);
						if(varValue instanceof Resource) {
							buffer.append(get().getLabel((Resource)varValue));
						}
						else if(varValue instanceof Literal) {
							buffer.append(varValue.asNode().getLiteralLexicalForm());
						}
						break;
					}
					else {
						varEnd++;
					}
				}
				i = varEnd;
			}
			else {
				buffer.append(labelTemplate.charAt(i));
			}
		}
	}


	/**
	 * Takes a string that is (likely) in camelCase and attempts to produce an un-camel-cased version of that.
	 * Useful to generate labels from local names.
	 * @param camelCase  the input String
	 * @return the generated String
	 */
	public static String unCamelCase(String camelCase) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < camelCase.length(); i++) {
			char c = camelCase.charAt(i);
			if(c == '_') {
				result.append(' ');
			}
			else if (Character.isUpperCase(c)) {
				if (i > 0 && Character.isLowerCase(camelCase.charAt(i - 1))) {
					result.append(" ");
					char newChar;
					if (i < camelCase.length() - 1
							&& Character.isUpperCase(camelCase.charAt(i + 1))) {
						newChar = c;
					}
					else {
						newChar = Character.toLowerCase(c);
					}
					result.append(newChar);
				}
				else {
					result.append(c);
				}
			}
			else {
				result.append(c);
			}
		}
		return result.toString();
	}
}
