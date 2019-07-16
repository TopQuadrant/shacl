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

import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionFactory;
import org.topbraid.shacl.vocabulary.SH;

public class ExpressionConstraintExecutor implements ConstraintExecutor {

	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, List<RDFNode> focusNodes) {
		// TODO: optimize, currently produces a new NodeExpression each time
		NodeExpression expr = NodeExpressionFactory.get().create(constraint.getParameterValue());
		for(RDFNode focusNode : focusNodes) {
			engine.checkCanceled();
			for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
				List<RDFNode> results = expr.eval(valueNode, engine).toList();
				if(results.size() != 1 || !JenaDatatypes.TRUE.equals(results.get(0))) {
					Resource result = engine.createResult(SH.ValidationResult, constraint, focusNode);
					result.addProperty(SH.value, valueNode);
					result.addProperty(SH.sourceConstraint, constraint.getParameterValue());
					if(constraint.getParameterValue() instanceof Resource && ((Resource)constraint.getParameterValue()).hasProperty(SH.message)) {
						for(Statement s : ((Resource)constraint.getParameterValue()).listProperties(SH.message).toList()) {
							result.addProperty(SH.resultMessage, s.getObject());
						}
					}
					else if(constraint.getShapeResource().hasProperty(SH.message)) {
						for(Statement s : constraint.getShapeResource().listProperties(SH.message).toList()) {
							result.addProperty(SH.resultMessage, s.getObject());
						}
					}
					else {
						result.addProperty(SH.resultMessage, "Expression does not evaluate to true");
					}
				}
			}
		}
	}
}
