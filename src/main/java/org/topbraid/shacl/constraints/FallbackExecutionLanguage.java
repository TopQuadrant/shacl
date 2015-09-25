package org.topbraid.shacl.constraints;

import java.net.URI;
import java.util.Collections;

import org.topbraid.shacl.model.SHACLConstraint;
import org.topbraid.shacl.model.SHACLTemplateCall;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * An ExecutionLanguage that serves as fall-back, if a constraint or template does not define
 * any valid executable property such as sh:sparql.
 * This will simply report a failure that the constraint could not be evaluated.
 * 
 * @author Holger Knublauch
 */
public class FallbackExecutionLanguage implements ExecutionLanguage {

	
	@Override
	public boolean canExecuteConstraint(ConstraintExecutable executable) {
		return true;
	}

	
	@Override
	public boolean canExecuteScope(Resource executable) {
		return true;
	}


	@Override
	public void executeConstraint(Dataset dataset, Resource shape,
			URI shapesGraphURI, SHACLConstraint constraint, ConstraintExecutable executable,
			RDFNode focusNode, Model results) {
		Resource result = results.createResource(DASH.FailureResult);
		result.addProperty(SH.message, "No execution language found for constraint");
		result.addProperty(SH.sourceConstraint, constraint);
		result.addProperty(SH.sourceShape, shape);
		if(executable instanceof TemplateConstraintExecutable) {
			result.addProperty(SH.sourceTemplate, ((TemplateConstraintExecutable)executable).getResource());
		}
		if(focusNode != null) {
			result.addProperty(SH.focusNode, focusNode);
		}
	}


	@Override
	public Iterable<RDFNode> executeScope(Dataset dataset,
			Resource executable, SHACLTemplateCall templateCall) {
		return Collections.emptyList();
	}


	@Override
	public boolean isNodeInScope(RDFNode focusNode, Dataset dataset,
			Resource executable, SHACLTemplateCall templateCall) {
		return false;
	}
}
