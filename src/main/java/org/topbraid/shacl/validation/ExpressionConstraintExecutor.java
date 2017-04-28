package org.topbraid.shacl.validation;

import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionFactory;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaDatatypes;

public class ExpressionConstraintExecutor implements ConstraintExecutor {

	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, List<RDFNode> focusNodes) {
		// TODO: optimize, currently produces a new NodeExpression each time
		NodeExpression expr = NodeExpressionFactory.get().create(constraint.getParameterValue(), engine.getShapesGraph());
		for(RDFNode focusNode : focusNodes) {
			for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
				List<RDFNode> results = expr.eval(valueNode, engine);
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
