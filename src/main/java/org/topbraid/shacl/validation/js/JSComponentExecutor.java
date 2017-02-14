package org.topbraid.shacl.validation.js;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHConstraintComponent;
import org.topbraid.shacl.model.SHJSConstraint;
import org.topbraid.shacl.model.SHJSExecutable;
import org.topbraid.shacl.validation.Constraint;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.shacl.vocabulary.SHJS;

/**
 * Executes constraints based on a JavaScript validator.
 * 
 * @author Holger Knublauch
 */
public class JSComponentExecutor extends AbstractJSExecutor {

	@Override
	protected void addBindings(Constraint constraint, QuerySolutionMap bindings) {
		constraint.addBindings(bindings);
	}

	
	@Override
	protected SHJSExecutable getExecutable(Constraint constraint) {
		return constraint.getComponent().getValidator(SHJS.JSValidator, constraint.getContext()).as(SHJSConstraint.class);
	}


	protected List<RDFNode> getValueNodes(ValidationEngine validationEngine, Constraint constraint, QuerySolutionMap bindings, RDFNode focusNode) {
		SHConstraintComponent component = constraint.getComponent();
		Resource context = constraint.getContext();
		Resource validatorResource = component.getValidator(SHJS.JSValidator, context);
		if(SH.PropertyShape.equals(context)) {
			if(component.hasProperty(SH.propertyValidator, validatorResource)) {
				bindings.add("path", constraint.getShapeResource().getRequiredProperty(SH.path).getObject());
				List<RDFNode> valueNodes = new ArrayList<>();
				valueNodes.add(null);
				return valueNodes;
			}
			else {
				return validationEngine.getValueNodes(constraint, focusNode);
			}
		}
		else {
			bindings.add("value", focusNode);
			return Collections.singletonList(focusNode);
		}
	}
}
