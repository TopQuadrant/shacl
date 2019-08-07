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
package org.topbraid.shacl.validation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.util.RecursionGuard;

/**
 * Implements the special handling of sh:property by recursively calling the validator
 * against the provided property shape.
 * 
 * @author Holger Knublauch
 */
class PropertyConstraintExecutor implements ConstraintExecutor {

	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		Node propertyShape = constraint.getParameterValue().asNode();
		if(constraint.getShapeResource().isPropertyShape()) {
			for(RDFNode focusNode : focusNodes) {
				Collection<RDFNode> valueNodes = engine.getValueNodes(constraint, focusNode);
				executeHelper(engine, valueNodes, propertyShape);
				engine.checkCanceled();
			}
		}
		else {
			executeHelper(engine, focusNodes, propertyShape);
		}
	}

	
	private void executeHelper(ValidationEngine engine, Collection<RDFNode> valueNodes, Node propertyShape) {
		List<RDFNode> doNodes = new LinkedList<>();
		for(RDFNode focusNode : valueNodes) {
			if(!RecursionGuard.start(focusNode.asNode(), propertyShape)) {
				doNodes.add(focusNode);
			}
		}
		try {
			engine.validateNodesAgainstShape(doNodes, propertyShape);
		}
		finally {
			for(RDFNode valueNode : doNodes) {
				RecursionGuard.end(valueNode.asNode(), propertyShape);
			}
		}
	}
}
