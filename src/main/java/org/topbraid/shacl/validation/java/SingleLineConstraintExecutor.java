package org.topbraid.shacl.validation.java;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;

/**
 * Validator for dash:singleLine constraints.
 * 
 * @author Holger Knublauch
 */
class SingleLineConstraintExecutor extends AbstractNativeConstraintExecutor {
	
	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		if(JenaDatatypes.TRUE.equals(constraint.getParameterValue())) {
			long startTime = System.currentTimeMillis();
			long valueNodeCount = 0;
			for(RDFNode focusNode : focusNodes) {
				for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
					valueNodeCount++;
					if(!valueNode.isLiteral()) {					
						engine.createValidationResult(constraint, focusNode, valueNode, () -> "Not a literal");
					}
					else {
						String lex = valueNode.asNode().getLiteralLexicalForm();
						if(lex.contains("\n") || lex.contains("\r")) {
							engine.createValidationResult(constraint, focusNode, valueNode, () -> "Must not contain line breaks");					
						}
					}
				}
				engine.checkCanceled();
			}
			addStatistics(engine, constraint, startTime, focusNodes.size(), valueNodeCount);
		}
	}
}
