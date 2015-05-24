package org.topbraid.shacl.constraints;

import java.util.LinkedList;
import java.util.List;

import org.topbraid.shacl.constraints.sparql.SPARQLExecutionLanguage;

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
	
	
	public ExecutionLanguage getLanguage(NativeConstraintExecutable executable) {
		for(ExecutionLanguage lang : languages) {
			if(lang.canExecuteNative(executable)) {
				return lang;
			}
		}
		return null;
	}
	
	
	public ExecutionLanguage getLanguage(TemplateConstraintExecutable executable) {
		for(ExecutionLanguage lang : languages) {
			if(lang.canExecuteTemplate(executable)) {
				return lang;
			}
		}
		return null;
	}
}
