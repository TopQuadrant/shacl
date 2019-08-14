package org.topbraid.shacl.validation.java;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;

class HasValueConstraintExecutor extends AbstractNativeConstraintExecutor {

	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		RDFNode hasValue = constraint.getParameterValue();
		for(RDFNode focusNode : focusNodes) {
			Collection<RDFNode> valueNodes = engine.getValueNodes(constraint, focusNode);
			if(!valueNodes.contains(hasValue)) {
				engine.createValidationResult(constraint, focusNode, null, () -> "Does not have value " + engine.getLabelFunction().apply(hasValue));
			}
			engine.checkCanceled();
		}
		addStatistics(constraint, startTime);
	}
}
