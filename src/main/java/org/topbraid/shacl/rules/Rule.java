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
package org.topbraid.shacl.rules;

import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.engine.Shape;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Represents a single rule in executable "pre-compiled" form.
 * 
 * @author Holger Knublauch
 */
public abstract class Rule {
	
	private Number order;
	
	private Resource resource;
	
	
	protected Rule(Resource resource) {
		this.resource = resource;
		order = 0;
		Statement s = resource.getProperty(SH.order);
		if(s != null && s.getObject().isLiteral()) {
			order = (Number) s.getLiteral().getValue();
		}
	}
	
	/**
	 * Executes this rule, calling <code>ruleEngine.infer()</code> to add triples.
	 * @param ruleEngine  the RuleEngine to operate on
	 * @param focusNodes  the list of focus nodes for this execution
	 */
	public abstract void execute(RuleEngine ruleEngine, List<RDFNode> focusNodes, Shape shape);
	
	
	public String getLabel() {
		Statement s = resource.getProperty(RDFS.label);
		if(s != null && s.getObject().isLiteral()) {
			return s.getString();
		}
		else {
			return null;
		}
	}
	
	
	public String getLabelStart(String type) {
		Number index = getOrder();
		int conditionCount = getResource().listProperties(SH.condition).toList().size();
		return type + " rule (" + (index.doubleValue() == 0 ? "0" : index) + 
				(conditionCount > 0 ? (", with " + conditionCount + " conditions") : "") + "): ";
	}
	
	
	public Number getOrder() {
		return order;
	}
	
	
	public Resource getResource() {
		return resource;
	}
}
