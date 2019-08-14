package org.topbraid.shacl.validation.java;

import java.util.Collection;
import java.util.Set;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;

class EqualsConstraintExecutor extends AbstractNativeConstraintExecutor {

	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		Property equalsPredicate = constraint.getParameterValue().as(Property.class);
		for(RDFNode focusNode : focusNodes) {
			if(focusNode instanceof Resource) {
				Collection<RDFNode> valueNodes = engine.getValueNodes(constraint, focusNode);
				Set<RDFNode> otherNodes = ((Resource)focusNode).listProperties(equalsPredicate).mapWith(s -> s.getObject()).toSet();
				for(RDFNode valueNode : valueNodes) {
					if(!otherNodes.contains(valueNode)) {
						engine.createValidationResult(constraint, focusNode, valueNode, () -> "Does not have value at property " + engine.getLabelFunction().apply(equalsPredicate));
					}
				}
				for(RDFNode otherNode : otherNodes) {
					if(!valueNodes.contains(otherNode)) {
						engine.createValidationResult(constraint, focusNode, otherNode, () -> "Expected value from property " + engine.getLabelFunction().apply(equalsPredicate));
					}
				}
			}
			engine.checkCanceled();
		}
		addStatistics(constraint, startTime);
	}
}
