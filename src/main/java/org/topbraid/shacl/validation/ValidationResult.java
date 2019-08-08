package org.topbraid.shacl.validation;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * A validation result, as produced by the validation engine.
 * 
 * @author Holger Knublauch
 */
public interface ValidationResult {

	RDFNode getFocusNode();
	
	String getMessage();
	
	Collection<RDFNode> getMessages();
	
	Resource getPath();
	
	Resource getSeverity();
	
	Resource getSourceConstraint();
	
	Resource getSourceConstraintComponent();
	
	Resource getSourceShape();
	
	RDFNode getValue();
}
