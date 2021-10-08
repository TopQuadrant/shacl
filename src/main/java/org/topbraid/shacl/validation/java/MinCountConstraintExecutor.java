package org.topbraid.shacl.validation.java;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;

/**
 * Validator for sh:minCount constraints.
 * 
 * @author Holger Knublauch
 */
class MinCountConstraintExecutor extends AbstractNativeConstraintExecutor {
	
	private int minCount;
	
	MinCountConstraintExecutor(Constraint constraint) {
		this.minCount = constraint.getParameterValue().asLiteral().getInt();
	}

	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		long valueNodeCount = 0;
		for(RDFNode focusNode : focusNodes) {
			int count = engine.getValueNodes(constraint, focusNode).size();
			valueNodeCount += count;
			if(count < minCount) {
				engine.createValidationResult(constraint, focusNode, null, () -> "Property needs to have at least " + minCount + " value" + (minCount == 1 ? "" : "s") + (count > 0 ? ", but found " + count : ""));
			}
			engine.checkCanceled();
		}
		addStatistics(engine, constraint, startTime, focusNodes.size(), valueNodeCount);
	}
}
