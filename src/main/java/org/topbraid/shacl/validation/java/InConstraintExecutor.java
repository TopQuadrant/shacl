package org.topbraid.shacl.validation.java;

import java.util.Collection;
import java.util.Set;

import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;

class InConstraintExecutor extends AbstractNativeConstraintExecutor {
	
	private Set<RDFNode> ins;
	
	InConstraintExecutor(Constraint constraint) {
		RDFList list = constraint.getParameterValue().as(RDFList.class);
		this.ins = list.iterator().toSet();
	}

	
	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		for(RDFNode focusNode : focusNodes) {
			for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {				
				if(!ins.contains(valueNode)) {
					engine.createValidationResult(constraint, focusNode, valueNode, () -> "Not a value from the sh:in enumeration");
				}
			}
			engine.checkCanceled();
		}
		addStatistics(constraint, startTime);
	}
}
