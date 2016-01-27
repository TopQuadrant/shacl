package org.topbraid.shacl.constraints;

import java.util.LinkedList;
import java.util.List;

import org.topbraid.shacl.constraints.sparql.SPARQLExecutionLanguage;

import org.apache.jena.rdf.model.Resource;

/**
 * Selects a suitable execution language for a given executable.
 * 
 * @author Holger Knublauch
 */
public class ExecutionLanguageSelector {

	private static ExecutionLanguageSelector singleton = new ExecutionLanguageSelector();
	
	public static ExecutionLanguageSelector get() {
		return singleton;
	}

	
	private final List<ExecutionLanguage> languages = new LinkedList<ExecutionLanguage>();
	
	
	public ExecutionLanguageSelector() {
		languages.add(SPARQLExecutionLanguage.get());
		languages.add(new FallbackExecutionLanguage());
	}

	
	/**
	 * Registers a new ExecutionLanguage to the registry.
	 * @param language  the language to add
	 */
	public void addLanguage(ExecutionLanguage language) {
		languages.add(1, language);
	}
	
	
	public ExecutionLanguage getLanguageForConstraint(ConstraintExecutable executable) {
		for(ExecutionLanguage lang : languages) {
			if(lang.canExecuteConstraint(executable)) {
				return lang;
			}
		}
		return null;
	}
	
	
	public ExecutionLanguage getLanguageForScope(Resource executable) {
		for(ExecutionLanguage lang : languages) {
			if(lang.canExecuteScope(executable)) {
				return lang;
			}
		}
		return null;
	}
}
