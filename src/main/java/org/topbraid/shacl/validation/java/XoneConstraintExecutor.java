package org.topbraid.shacl.validation.java;

import java.util.Collection;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.ValidationEngine;

class XoneConstraintExecutor extends AbstractShapeListConstraintExecutor {

	XoneConstraintExecutor(Constraint constraint) {
		super(constraint);
	}

	
	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {

		long startTime = System.currentTimeMillis();

		for(RDFNode focusNode : focusNodes) {
			for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
				long count = shapes.stream().filter(shape -> {
					Model nestedResults = hasShape(engine, constraint, focusNode, valueNode, shape, true);
					return nestedResults != null;
				}).count();
				if(count != 1) {
					engine.createValidationResult(constraint, focusNode, valueNode, () -> "Value has " + count + " shapes out of " + shapes.size() + " in the sh:xone enumeration");
				}
			}
		}

		addStatistics(constraint, startTime);
	}
}
