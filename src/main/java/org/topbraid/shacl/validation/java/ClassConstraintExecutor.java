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

/**
 * Validator for sh:class constraints.
 * 
 * @author Holger Knublauch
 */
class ClassConstraintExecutor extends AbstractNativeConstraintExecutor {

	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		long valueNodeCount;
		Resource classNode = (Resource) constraint.getParameterValue();
		if(!constraint.getShape().isNodeShape()) {
			valueNodeCount = 0;
			for(RDFNode focusNode : focusNodes) {
				if(!focusNode.isLiteral()) {
					for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
						valueNodeCount++;
						validate(constraint, engine, classNode, focusNode, valueNode);
					}
				}
				engine.checkCanceled();
			}
		}
		else {
			valueNodeCount = focusNodes.size();
			for(RDFNode focusNode : focusNodes) {
				validate(constraint, engine, classNode, focusNode, focusNode);
				engine.checkCanceled();
			}
		}
		addStatistics(engine, constraint, startTime, focusNodes.size(), valueNodeCount);
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
