package org.topbraid.shacl.validation.java;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;

abstract class AbstractLengthConstraintExecutor extends AbstractNativeConstraintExecutor {
	
	private int expectedLength;
	
	AbstractLengthConstraintExecutor(Constraint constraint) {
		expectedLength = constraint.getParameterValue().asLiteral().getInt();
	}
	
	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		String message = "Value has " + getComparisonString() + " than " + expectedLength + " characters";
		int valueNodeCount;
		if(constraint.getShapeResource().isPropertyShape()) {
			valueNodeCount = 0;
			for(RDFNode focusNode : focusNodes) {
				for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
					valueNodeCount++;
					validate(constraint, engine, message, expectedLength, focusNode, valueNode);
				}
				engine.checkCanceled();
			}
		}
		else {
			for(RDFNode focusNode : focusNodes) {
				validate(constraint, engine, message, expectedLength, focusNode, focusNode);
				engine.checkCanceled();
			}
			valueNodeCount = focusNodes.size();
		}
		addStatistics(engine, constraint, startTime, focusNodes.size(), valueNodeCount);
	}
	
	
	protected abstract String getComparisonString();
	
	
	protected abstract boolean isInvalidLength(int actualLength, int expectedLength);


	private void validate(Constraint constraint, ValidationEngine engine, String message, int length, RDFNode focusNode, RDFNode valueNode) {
		if(valueNode.isAnon() || 
				(valueNode.isURIResource() && isInvalidLength(valueNode.asNode().getURI().length(), length)) || 
				(valueNode.isLiteral() && isInvalidLength(valueNode.asNode().getLiteralLexicalForm().length(), length))) {
			engine.createValidationResult(constraint, focusNode, valueNode, () -> message);
		}
	}
}
