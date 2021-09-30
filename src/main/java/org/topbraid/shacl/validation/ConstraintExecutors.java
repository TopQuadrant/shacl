/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
package org.topbraid.shacl.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.java.JavaConstraintExecutors;
import org.topbraid.shacl.validation.sparql.SPARQLConstraintExecutor;
import org.topbraid.shacl.validation.sparql.SPARQLValidationLanguage;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

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
	
	private List<ValidationLanguage> languages = new ArrayList<>();
	
	private Map<Resource,Function<Constraint,ConstraintExecutor>> specialExecutors = new HashMap<>();

	
	public ConstraintExecutors() {
		addSpecialExecutor(SH.PropertyConstraintComponent, constraint -> new PropertyConstraintExecutor());
		addSpecialExecutor(DASH.ParameterConstraintComponent, constraint -> new PropertyConstraintExecutor());
		addSpecialExecutor(SH.SPARQLConstraintComponent, constraint -> new SPARQLConstraintExecutor(constraint));
		addSpecialExecutor(SH.ExpressionConstraintComponent, constraint -> new ExpressionConstraintExecutor());

		JavaConstraintExecutors.install(this);

		addLanguage(SPARQLValidationLanguage.get());
	}
	
	
	public void addLanguage(ValidationLanguage language) {
		languages.add(language);
	}
	
	
	public void addSpecialExecutor(Resource constraintComponent, Function<Constraint,ConstraintExecutor> executor) {
		specialExecutors.put(constraintComponent, executor);
	}
	
	
	public ConstraintExecutor getExecutor(Constraint constraint) {

		Function<Constraint,ConstraintExecutor> special = specialExecutors.get(constraint.getComponent());
		if(special != null) {
			return special.apply(constraint);
		}
		
		for(ValidationLanguage language : languages) {
			if(language.canExecute(constraint)) {
				return language.createExecutor(constraint);
			}
		}

		return null;
	}
	
	
	public void removeSpecialExecutor(Resource constraintComponent) {
		specialExecutors.remove(constraintComponent);
	}
}
