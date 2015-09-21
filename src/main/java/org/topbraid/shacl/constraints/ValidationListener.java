package org.topbraid.shacl.constraints;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * An interface for objects that wish to be informed about the progress of a SHACL constraint validation.
 * 
 * @author Holger Knublauch
 */
public interface ValidationListener {
	
	void validationFinished(Resource shape, ConstraintExecutable executable, RDFNode focusNode, ExecutionLanguage lang, Model results);

	
	void validationStarting(Resource shape, ConstraintExecutable executable, RDFNode focusNode, ExecutionLanguage lang, Model results);
}
