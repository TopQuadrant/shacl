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

import org.apache.jena.rdf.model.Model;

/**
 * A singleton object that can create ValidationSuggestionGenerators.
 * 
 * @author Holger Knublauch
 */
public class ValidationSuggestionGeneratorFactory {
	
	private static ValidationSuggestionGeneratorFactory singleton = new ValidationSuggestionGeneratorFactory();
	
	public static ValidationSuggestionGeneratorFactory get() {
		return singleton;
	}
	
	public static void set(ValidationSuggestionGeneratorFactory value) {
		singleton = value;
	}
	
	
	/**
	 * Default implementation returns nothing - no implementation is provided as part of the
	 * open source package.
	 * @param dataModel  the data graph to operate on
	 * @param shapesModel  the shapes graph to operate on
	 * @return a {@link ValidationSuggestionGenerator} or null
	 */
	public ValidationSuggestionGenerator createValidationSuggestionGenerator(Model dataModel, Model shapesModel) {
		return null;
	}
}
