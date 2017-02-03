package org.topbraid.shacl.constraints;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.constraints.sparql.SPARQLExecutionLanguage;
import org.topbraid.shacl.js.JSExecutionLanguage;
import org.topbraid.shacl.util.SHACLUtil;

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
		
		addLanguage(new JSExecutionLanguage());
	}

	
	/**
	 * Registers a new ExecutionLanguage to the registry.
	 * @param language  the language to add
	 */
	public void addLanguage(ExecutionLanguage language) {
		languages.add(1, language);
		if(language.getParameter() != null) {
			SHACLUtil.addConstraintProperty(language.getParameter());
		}
	}
	
	
	public ExecutionLanguage getLanguageForConstraint(ConstraintExecutable executable) {
		for(ExecutionLanguage lang : languages) {
			if(lang.canExecuteConstraint(executable)) {
				return lang;
			}
		}
		return null;
	}
	
	
	public ExecutionLanguage getLanguageForParameter(Property parameter) {
		for(int i = 0; i < languages.size(); i++) {
			if(parameter.equals(languages.get(i).getParameter())) {
				return languages.get(i);
			}
		}
		return null;
	}
	
	
	public ExecutionLanguage getLanguageForTarget(Resource executable) {
		for(ExecutionLanguage lang : languages) {
			if(lang.canExecuteTarget(executable)) {
				return lang;
			}
		}
		return null;
	}
	
	
	public boolean isConstraintComponentWithLanguage(Resource cc) {
		for(ExecutionLanguage lang : languages) {
			if(cc.equals(lang.getConstraintComponent())) {
				return true;
			}
		}
		return false;
	}
}
