package org.topbraid.shacl.constraints;

import org.topbraid.shacl.model.SHACLConstraint;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A processor that can execute ConstraintExecutables.
 * The default implementation uses SPARQL, based on sh:sparql.
 * 
 * @author Holger Knublauch
 */
public interface ExecutionLanguage {
	
	boolean canExecuteNative(NativeConstraintExecutable executable);
	
	boolean canExecuteTemplate(TemplateConstraintExecutable executable);
	
	void executeNative(Dataset dataset, Resource shape, Resource shapesGraph, Model results, SHACLConstraint constraint, Resource focusNode, Property selectorProperty, Resource selectorObject, NativeConstraintExecutable executable);
	
	void executeTemplate(Dataset dataset, Resource shape, Resource shapesGraph, Model results, SHACLConstraint constraint, Resource focusNode, Property selectorProperty, Resource selectorObject, TemplateConstraintExecutable executable);
}
