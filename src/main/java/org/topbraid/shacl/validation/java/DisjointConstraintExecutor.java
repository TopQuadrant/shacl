package org.topbraid.shacl.validation.java;

import java.util.Collection;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;

class DisjointConstraintExecutor extends AbstractNativeConstraintExecutor {

	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		Property disjointPredicate = constraint.getParameterValue().as(Property.class);
		for(RDFNode focusNode : focusNodes) {
			if(focusNode instanceof Resource) {
				for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
					if(((Resource)focusNode).hasProperty(disjointPredicate, valueNode)) {
						engine.createValidationResult(constraint, focusNode, valueNode, () -> "Property must not share any values with " + engine.getLabelFunction().apply(disjointPredicate));
					}
				}
			}
			engine.checkCanceled();
		}
		addStatistics(constraint, startTime);
	}
}
