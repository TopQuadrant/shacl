package org.topbraid.shacl.validation.java;

import java.util.Collection;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.ValidationEngine;

/**
 * Validator for sh:and constraints.
 * 
 * @author Holger Knublauch
 */
class AndConstraintExecutor extends AbstractShapeListConstraintExecutor {

	AndConstraintExecutor(Constraint constraint) {
		super(constraint);
	}

	
	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {

		long startTime = System.currentTimeMillis();

		long valueNodeCount = 0;
		for(RDFNode focusNode : focusNodes) {
			for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
				valueNodeCount++;
				if(shapes.stream().anyMatch(shape -> {
					Model nestedResults = hasShape(engine, constraint, focusNode, valueNode, shape, true);
					return nestedResults != null;
				})) {
					engine.createValidationResult(constraint, focusNode, valueNode, () -> "Value must have all of the following shapes: " + shapeLabelsList(engine));
				}
			}
		}

		addStatistics(engine, constraint, startTime, focusNodes.size(), valueNodeCount);
	}
}
