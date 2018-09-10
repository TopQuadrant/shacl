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

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.shacl.engine.Shape;

/**
 * Represents a single rule in executable "pre-compiled" form.
 * 
 * @author Holger Knublauch
 */
public interface Rule {
	
	/**
	 * Executes this rule, calling <code>ruleEngine.infer()</code> to add triples.
	 * @param ruleEngine  the RuleEngine to operate on
	 * @param focusNodes  the list of focus nodes for this execution
	 * @param shape  the context shape
	 */
	void execute(RuleEngine ruleEngine, List<RDFNode> focusNodes, Shape shape);
	
	
	// Used for statistics only, to identify the origin of an inference
	Node getContextNode();

	
	Number getOrder();
}
