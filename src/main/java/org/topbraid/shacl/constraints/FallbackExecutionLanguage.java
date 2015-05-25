package org.topbraid.shacl.constraints;

import org.topbraid.shacl.model.SHACLConstraint;
import org.topbraid.shacl.vocabulary.SH;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
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
	public boolean canExecuteNative(NativeConstraintExecutable executable) {
		return true;
	}

	
	@Override
	public boolean canExecuteTemplate(TemplateConstraintExecutable executable) {
		return true;
	}

	
	@Override
	public void executeNative(Dataset dataset, Resource shape,
			Resource shapesGraph, Model results, SHACLConstraint constraint,
			Resource focusNode, Property selectorProperty,
			Resource selectorObject, NativeConstraintExecutable executable) {
		Resource vio = results.createResource(SH.Warning);
		vio.addProperty(SH.message, "No execution language found for constraint");
		vio.addProperty(SH.source, constraint);
		if(focusNode != null) {
			vio.addProperty(SH.root, focusNode);
		}
	}

	
	@Override
	public void executeTemplate(Dataset dataset, Resource shape,
			Resource shapesGraph, Model results, SHACLConstraint constraint,
			Resource focusNode, Property selectorProperty,
			Resource selectorObject, TemplateConstraintExecutable executable) {
		Resource vio = results.createResource(SH.Warning);
		vio.addProperty(SH.message, "No execution language found for template " + executable.getTemplate());
		vio.addProperty(SH.source, constraint);
		if(focusNode != null) {
			vio.addProperty(SH.root, focusNode);
		}
	}
}
