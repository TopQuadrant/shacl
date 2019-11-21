package org.topbraid.shacl.validation.java;

import java.util.Collection;
import java.util.function.Predicate;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ClassesCache;
import org.topbraid.shacl.validation.ValidationEngine;

class ClassConstraintExecutor extends AbstractNativeConstraintExecutor {

	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		Resource classNode = (Resource) constraint.getParameterValue();
		if(!constraint.getShape().isNodeShape()) {
			for(RDFNode focusNode : focusNodes) {
				if(!focusNode.isLiteral()) {
					for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
						validate(constraint, engine, classNode, focusNode, valueNode);
					}
				}
				engine.checkCanceled();
			}
		}
		else {
			for(RDFNode focusNode : focusNodes) {
				validate(constraint, engine, classNode, focusNode, focusNode);
				engine.checkCanceled();
			}
		}
		addStatistics(constraint, startTime);
	}


	private void validate(Constraint constraint, ValidationEngine engine, Resource classNode, RDFNode focusNode, RDFNode valueNode) {
		if(valueNode.isLiteral()) {
			engine.createValidationResult(constraint, focusNode, valueNode, () -> "Value must be an instance of " + engine.getLabel(classNode));			
		}
		else {			
			ClassesCache cache = engine.getClassesCache();
			if(cache != null) {
				Predicate<Resource> pred = cache.getPredicate(classNode.inModel(valueNode.getModel()));
				if(!pred.test((Resource)valueNode)) {
					engine.createValidationResult(constraint, focusNode, valueNode, () -> "Value must be an instance of " + engine.getLabel(classNode));
				}
			}
			else if(!JenaUtil.hasIndirectType((Resource)valueNode, classNode)) {
				// No cache: possibly walk superclasses for each call
				engine.createValidationResult(constraint, focusNode, valueNode, () -> "Value must be an instance of " + engine.getLabel(classNode));
			}
		}
	}
}
