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

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHPropertyShape;

/**
 * Implements the special handling of sh:property by recursively calling the validator
 * against the provided property shape.
 * 
 * @author Holger Knublauch
 */
class PropertyConstraintExecutor implements ConstraintExecutor {

	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, List<RDFNode> focusNodes) {
		SHPropertyShape propertyShape = SHFactory.asPropertyShape(constraint.getParameterValue());
		if(constraint.getShapeResource().isPropertyShape()) {
			List<RDFNode> valueNodes = new LinkedList<RDFNode>();
			for(RDFNode focusNode : focusNodes) {
				valueNodes.addAll(engine.getValueNodes(constraint, focusNode));
			}
			engine.validateNodesAgainstShape(valueNodes, propertyShape.asNode());
		}
		else {
			engine.validateNodesAgainstShape(focusNodes, propertyShape.asNode());
		}
	}
}
