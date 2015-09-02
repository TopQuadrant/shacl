package org.topbraid.shacl.constraints;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Base class for ResourceConstraintValidator and ModelConstraintValidator.
 * 
 * @author Holger Knublauch
 */
public class AbstractConstraintValidator {

	private List<ValidationListener> listeners = new ArrayList<ValidationListener>();
	
	
	public void addValidationListener(ValidationListener listener) {
		listeners.add(listener);
	}
	
	
	protected void notifyValidationFinished(Resource shape, ConstraintExecutable executable, Resource focusNode, ExecutionLanguage lang, Model results) {
		for(ValidationListener listener : listeners) {
			listener.validationFinished(shape, executable, focusNode, lang, results);
		}
	}
	
	
	protected void notifyValidationStarting(Resource shape, ConstraintExecutable executable, Resource focusNode, ExecutionLanguage lang, Model results) {
		for(ValidationListener listener : listeners) {
			listener.validationStarting(shape, executable, focusNode, lang, results);
		}
	}
	
	
	public void removeValidationListener(ValidationListener listener) {
		listeners.remove(listener);
	}
}
