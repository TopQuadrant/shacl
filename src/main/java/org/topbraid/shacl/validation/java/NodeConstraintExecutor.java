package org.topbraid.shacl.validation.java;

import java.util.Collection;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.validation.sparql.AbstractSPARQLExecutor;

/**
 * Validator for sh:node constraints.
 * 
 * @author Holger Knublauch
 */
class NodeConstraintExecutor extends AbstractNativeConstraintExecutor {

	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		RDFNode shape = constraint.getParameterValue();
		long valueNodeCount = 0;
		for(RDFNode focusNode : focusNodes) {
			for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
				valueNodeCount++;
				Model nestedResults = hasShape(engine, constraint, focusNode, valueNode, shape, false);
				if(nestedResults != null) {
					Resource result = engine.createValidationResult(constraint, focusNode, valueNode, () -> "Value does not have shape " + engine.getLabelFunction().apply(shape));
					if(engine.getConfiguration().getReportDetails()) {
						AbstractSPARQLExecutor.addDetails(result, nestedResults);
					}
				}
			}
			engine.checkCanceled();
		}
		addStatistics(engine, constraint, startTime, focusNodes.size(), valueNodeCount);
	}
}
