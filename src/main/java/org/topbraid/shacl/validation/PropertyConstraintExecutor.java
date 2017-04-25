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
