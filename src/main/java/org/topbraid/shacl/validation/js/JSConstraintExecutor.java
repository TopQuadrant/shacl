package org.topbraid.shacl.validation.js;

import java.util.List;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.shacl.model.SHJSConstraint;
import org.topbraid.shacl.model.SHJSExecutable;
import org.topbraid.shacl.validation.ConstraintExecutor;
import org.topbraid.shacl.validation.Constraint;
import org.topbraid.shacl.validation.ValidationEngine;

/**
 * Executes sh:js constraints.
 * 
 * @author Holger Knublauch
 */
public class JSConstraintExecutor extends AbstractJSExecutor implements ConstraintExecutor {

	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine validationEngine, List<RDFNode> focusNodes) {
		
		SHJSConstraint js = constraint.getParameterValue().as(SHJSConstraint.class);
		
		if(js.isDeactivated()) {
			return;
		}

		super.executeConstraint(constraint, validationEngine, focusNodes);
	}


	@Override
	protected void addBindings(Constraint constraint, QuerySolutionMap bindings) {
		// Do nothing
	}

	
	@Override
	protected SHJSExecutable getExecutable(Constraint constraint) {
		return constraint.getParameterValue().as(SHJSConstraint.class);
	}


	@Override
	protected List<RDFNode> getValueNodes(ValidationEngine validationEngine, Constraint constraint, QuerySolutionMap bindings, RDFNode focusNode) {
		return validationEngine.getValueNodes(constraint, focusNode);
	}
}
