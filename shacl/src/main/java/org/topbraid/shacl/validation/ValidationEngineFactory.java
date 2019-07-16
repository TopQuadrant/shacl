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

import java.net.URI;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.engine.ShapesGraph;

/**
 * A singleton that can be used to produce new ValidationEngines.
 * 
 * Implementations may install their own subclass to make some default modifications
 * such as attaching a monitor or label function.
 * 
 * @author Holger Knublauch
 */
public class ValidationEngineFactory {

	private static ValidationEngineFactory singleton = new ValidationEngineFactory();
	
	public static ValidationEngineFactory get() {
		return singleton;
	}
	
	public static void set(ValidationEngineFactory value) {
		singleton = value;
	}
	
	
	/**
	 * Constructs a new ValidationEngine.
	 * @param dataset  the Dataset to operate on
	 * @param shapesGraphURI  the URI of the shapes graph (must be in the dataset)
	 * @param shapesGraph  the ShapesGraph with the shapes to validate against
	 * @param report  the sh:ValidationReport object in the results Model, or null to create a new one
	 * @return a new ValidationEngine
	 */
	public ValidationEngine create(Dataset dataset, URI shapesGraphURI, ShapesGraph shapesGraph, Resource report) {
		return new ValidationEngine(dataset, shapesGraphURI, shapesGraph, report);
	}
}
