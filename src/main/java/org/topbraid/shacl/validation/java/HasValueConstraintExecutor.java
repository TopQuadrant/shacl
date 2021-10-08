package org.topbraid.shacl.validation.java;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;

/**
 * Validator for sh:hasValue constraints.
 * 
 * @author Holger Knublauch
 */
class HasValueConstraintExecutor extends AbstractNativeConstraintExecutor {

	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		RDFNode hasValue = constraint.getParameterValue();
		long valueNodeCount = 0;
		for(RDFNode focusNode : focusNodes) {
			Collection<RDFNode> valueNodes = engine.getValueNodes(constraint, focusNode);
			valueNodeCount += valueNodes.size();
			if(!valueNodes.contains(hasValue)) {
				engine.createValidationResult(constraint, focusNode, null, () -> "Does not have value " + engine.getLabelFunction().apply(hasValue));
			}
			engine.checkCanceled();
		}
		addStatistics(engine, constraint, startTime, focusNodes.size(), valueNodeCount);
	}
}
