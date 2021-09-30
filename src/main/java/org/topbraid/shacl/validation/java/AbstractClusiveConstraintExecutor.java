package org.topbraid.shacl.validation.java;

import java.util.Collection;
import java.util.function.Predicate;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprNotComparableException;
import org.apache.jena.sparql.expr.NodeValue;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;

abstract class AbstractClusiveConstraintExecutor extends AbstractNativeConstraintExecutor {
	
	private Predicate<Integer> condition;
	
	private String operator;
	
	
	AbstractClusiveConstraintExecutor(Predicate<Integer> condition, String operator) {
		this.condition = condition;
		this.operator = operator;
	}


	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		int valueNodeCount = 0;
		NodeValue cmpValue = NodeValue.makeNode(constraint.getParameterValue().asNode());
		for(RDFNode focusNode : focusNodes) {
			for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
				valueNodeCount++;
				try {
			        NodeValue value = NodeValue.makeNode(valueNode.asNode());
					int c = NodeValue.compare(cmpValue, value);
					if (c == Expr.CMP_INDETERMINATE) {
						engine.createValidationResult(constraint, focusNode, valueNode, () -> "Indeterminant comparison with " + engine.getLabelFunction().apply(constraint.getParameterValue())); 
					}
					else if(!condition.test(c)) {
						engine.createValidationResult(constraint, focusNode, valueNode, () -> "Value is not " + operator + " " + engine.getLabelFunction().apply(constraint.getParameterValue())); 
					}
				}
				catch (ExprNotComparableException ex) {
					engine.createValidationResult(constraint, focusNode, valueNode, () -> "Cannot compare with " + engine.getLabelFunction().apply(constraint.getParameterValue())); 
				}
			}
			engine.checkCanceled();
		}
		addStatistics(engine, constraint, startTime, focusNodes.size(), valueNodeCount);
	}
}
