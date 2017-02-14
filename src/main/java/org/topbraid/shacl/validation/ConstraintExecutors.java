package org.topbraid.shacl.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.validation.js.JSConstraintExecutor;
import org.topbraid.shacl.validation.js.JSExecutionLanguage;
import org.topbraid.shacl.validation.sparql.SPARQLConstraintExecutor;
import org.topbraid.shacl.validation.sparql.SPARQLDerivedValuesExecutor;
import org.topbraid.shacl.validation.sparql.SPARQLExecutionLanguage;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.shacl.vocabulary.SHJS;

/**
 * Singleton managing the available ValidationLanguage instances.
 * 
 * @author Holger Knublauch
 */
public class ConstraintExecutors {

	private final static ConstraintExecutors singleton = new ConstraintExecutors();
	
	public static ConstraintExecutors get() {
		return singleton;
	}
	
	private List<ExecutionLanguage> languages = new ArrayList<>();
	
	private Map<Resource,SpecialConstraintExecutorFactory> specialExecutors = new HashMap<>();

	
	public ConstraintExecutors() {
		addSpecialExecutor(SH.PropertyConstraintComponent, new SpecialConstraintExecutorFactory() {
			@Override
			public ConstraintExecutor create(Constraint constraint) {
				return new PropertyConstraintExecutor();
			}
		});
		addSpecialExecutor(DASH.ParameterConstraintComponent, new SpecialConstraintExecutorFactory() {
			@Override
			public ConstraintExecutor create(Constraint constraint) {
				return new PropertyConstraintExecutor();
			}
		});
		addSpecialExecutor(SHJS.JSConstraintComponent, new SpecialConstraintExecutorFactory() {
			@Override
			public ConstraintExecutor create(Constraint constraint) {
				return new JSConstraintExecutor();
			}
		});
		addSpecialExecutor(SH.SPARQLConstraintComponent, new SpecialConstraintExecutorFactory() {
			@Override
			public ConstraintExecutor create(Constraint constraint) {
				return new SPARQLConstraintExecutor(constraint);
			}
		});
		addSpecialExecutor(SH.DerivedValuesConstraintComponent, new SpecialConstraintExecutorFactory() {
			@Override
			public ConstraintExecutor create(Constraint constraint) {
				return new SPARQLDerivedValuesExecutor(constraint);
			}
		});
		
		addLanguage(SPARQLExecutionLanguage.get());
		addLanguage(JSExecutionLanguage.get());
	}
	
	
	protected void addLanguage(ExecutionLanguage language) {
		languages.add(language);
	}
	
	
	protected void addSpecialExecutor(Resource constraintComponent, SpecialConstraintExecutorFactory executor) {
		specialExecutors.put(constraintComponent, executor);
	}
	
	
	public ConstraintExecutor getExecutor(Constraint constraint, ValidationEngine engine) {

		SpecialConstraintExecutorFactory special = specialExecutors.get(constraint.getComponent());
		if(special != null) {
			return special.create(constraint);
		}
		
		for(ExecutionLanguage language : languages) {
			if(language.canExecute(constraint, engine)) {
				return language.createExecutor(constraint, engine);
			}
		}

		return null;
	}
	
	
	/**
	 * Can be used to make the JavaScript engine the preferred implementation over SPARQL.
	 * By default, SPARQL is preferred.
	 * In cases where a constraint component has multiple validators, it would then chose
	 * the JavaScript one.
	 * @param value  true to make JS
	 */
	public void setJSPreferred(boolean value) {
		languages.remove(0);
		languages.remove(0);
		if(value) {
			languages.add(0, JSExecutionLanguage.get());
			languages.add(1, SPARQLExecutionLanguage.get());
		}
		else {
			languages.add(0, SPARQLExecutionLanguage.get());
			languages.add(1, JSExecutionLanguage.get());
		}
	}
}
