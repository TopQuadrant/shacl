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

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionFactory;
import org.topbraid.shacl.vocabulary.SH;

import java.util.Collection;
import java.util.List;

/**
 * Validator for sh:expression constraints, see <a href="https://w3c.github.io/shacl/shacl-af/#ExpressionConstraintComponent">ExpressionConstraintComponent</a>
 *
 * @author Holger Knublauch
 */
public class ExpressionConstraintExecutor implements ConstraintExecutor {

    @Override
    public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
        // TODO: optimize, currently produces a new NodeExpression each time
        NodeExpression expr = NodeExpressionFactory.get().create(constraint.getParameterValue());
        for (RDFNode focusNode : focusNodes) {
            engine.checkCanceled();
            for (RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
                List<RDFNode> results = expr.eval(valueNode, engine).toList();
                if (results.size() != 1 || !JenaDatatypes.TRUE.equals(results.get(0))) {
                    Resource result = engine.createValidationResult(constraint, focusNode, valueNode, () -> "Expression does not evaluate to true");
                    result.addProperty(SH.sourceConstraint, constraint.getParameterValue());
                    if (constraint.getParameterValue() instanceof Resource && ((Resource) constraint.getParameterValue()).hasProperty(SH.message)) {
                        for (Statement s : ((Resource) constraint.getParameterValue()).listProperties(SH.message).toList()) {
                            result.addProperty(SH.resultMessage, s.getObject());
                        }
                    }
                }
            }
        }
    }
}
