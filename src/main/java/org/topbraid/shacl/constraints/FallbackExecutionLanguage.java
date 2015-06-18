package org.topbraid.shacl.constraints;

import java.util.Collections;

import org.topbraid.shacl.model.SHACLConstraint;
import org.topbraid.shacl.model.SHACLTemplateCall;
import org.topbraid.shacl.vocabulary.SH;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * An ExecutionLanguage that serves as fall-back, if a constraint or template does not define
 * any valid executable property such as sh:sparql.
 * This will simply report a warning that the constraint could not be evaluated.
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
			Resource shapesGraph, SHACLConstraint constraint, ConstraintExecutable executable,
			Resource focusNode, Model results) {
		Resource vio = results.createResource(SH.Warning);
		vio.addProperty(SH.message, "No execution language found for constraint");
		vio.addProperty(SH.source, constraint);
		if(focusNode != null) {
			vio.addProperty(SH.root, focusNode);
		}
	}


	@Override
	public Iterable<Resource> executeScope(Dataset dataset,
			Resource executable, SHACLTemplateCall templateCall) {
		return Collections.emptyList();
	}


	@Override
	public boolean isNodeInScope(Resource focusNode, Dataset dataset,
			Resource executable, SHACLTemplateCall templateCall) {
		return false;
	}
}
