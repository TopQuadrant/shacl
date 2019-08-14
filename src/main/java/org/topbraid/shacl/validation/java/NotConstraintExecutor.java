package org.topbraid.shacl.validation.java;

import java.util.Collection;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;

class NotConstraintExecutor extends AbstractNativeConstraintExecutor {

	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		RDFNode shape = constraint.getParameterValue();
		for(RDFNode focusNode : focusNodes) {
			for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
				Model nestedResults = hasShape(engine, constraint, focusNode, valueNode, shape, false);
				if(nestedResults == null) {
					engine.createValidationResult(constraint, focusNode, valueNode, () -> "Value has shape " + engine.getLabelFunction().apply(shape));
				}
			}
			engine.checkCanceled();
		}
		addStatistics(constraint, startTime);
	}
}
