package org.topbraid.shacl.validation.java;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;

/**
 * Validator for sh:maxCount constraints.
 * 
 * @author Holger Knublauch
 */
class MaxCountConstraintExecutor extends AbstractNativeConstraintExecutor {
	
	private int maxCount;
	
	MaxCountConstraintExecutor(Constraint constraint) {
		this.maxCount = constraint.getParameterValue().asLiteral().getInt();
	}

	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		long valueNodeCount = 0;
		for(RDFNode focusNode : focusNodes) {
			// Here we could theoretically only count until the maxCount is reached, but then the error message would not be as informative
			int count = engine.getValueNodes(constraint, focusNode).size();
			valueNodeCount += count;
			if(count > maxCount) {
				engine.createValidationResult(constraint, focusNode, null, () -> "Property may only have " + maxCount + " value" + (maxCount == 1 ? "" : "s") + ", but found " + count);
			}
			engine.checkCanceled();
		}
		addStatistics(engine, constraint, startTime, focusNodes.size(), valueNodeCount);
	}
}
