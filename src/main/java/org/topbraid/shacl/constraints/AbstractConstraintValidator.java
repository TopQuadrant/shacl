package org.topbraid.shacl.constraints;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * Base class for ResourceConstraintValidator and ModelConstraintValidator.
 * 
 * @author Holger Knublauch
 */
public class AbstractConstraintValidator {
	
	private static ThreadLocal<Model> currentResultsModel = new ThreadLocal<Model>();
	
	public static Model getCurrentResultsModel() {
		return currentResultsModel.get();
	}
	
	protected void setCurrentResultsModel(Model value) {
		if(value != null) {
			currentResultsModel.set(value);
		}
		else {
			currentResultsModel.remove();
		}
	}

	private List<ValidationListener> listeners = new ArrayList<ValidationListener>();
	
	private Model oldResultsModel;
	
	
	public void addValidationListener(ValidationListener listener) {
		listeners.add(listener);
	}
	
	
	protected void notifyValidationFinished(Resource shape, ConstraintExecutable executable, RDFNode focusNode, ExecutionLanguage lang, Model results) {
		for(ValidationListener listener : listeners) {
			listener.validationFinished(shape, executable, focusNode, lang, results);
		}
		if(oldResultsModel != null) {
			currentResultsModel.set(oldResultsModel);
		}
		else {
			currentResultsModel.remove();
		}
	}
	
	
	protected void notifyValidationStarting(Resource shape, ConstraintExecutable executable, RDFNode focusNode, ExecutionLanguage lang, Model results) {
		oldResultsModel = currentResultsModel.get();
		currentResultsModel.set(results);
		for(ValidationListener listener : listeners) {
			listener.validationStarting(shape, executable, focusNode, lang, results);
		}
	}
	
	
	public void removeValidationListener(ValidationListener listener) {
		listeners.remove(listener);
	}
}
