package org.topbraid.shacl.validation.js;

import java.util.List;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.model.SHJSConstraint;
import org.topbraid.shacl.model.SHJSExecutable;
import org.topbraid.shacl.validation.ConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

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
	protected String getLabel(Constraint constraint) {
		return "JavaScript Constraint " + JenaUtil.getStringProperty((Resource)constraint.getParameterValue(), SH.jsFunctionName);
	}


	@Override
	protected List<RDFNode> getValueNodes(ValidationEngine validationEngine, Constraint constraint, QuerySolutionMap bindings, RDFNode focusNode) {
		return validationEngine.getValueNodes(constraint, focusNode);
	}
}
