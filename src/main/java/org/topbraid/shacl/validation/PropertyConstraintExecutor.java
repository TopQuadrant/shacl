package org.topbraid.shacl.validation;

import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
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
		engine.validateNodesAgainstShape(focusNodes, propertyShape.asNode());
	}
}
