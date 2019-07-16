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

import java.util.function.Function;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * An interface for objects that can produce suggestions for a given results graph.
 * 
 * @author Holger Knublauch
 */
public interface ValidationSuggestionGenerator {
	
	/**
	 * Adds dash:suggestion triples for all result resource in the given results Model.
	 * @param results  the results Model
	 * @param maxCount  the maximum number of suggestions to produce per result
	 * @param labelFunction  an optional function producing labels of nodes
	 * @return the number of suggestions that were created
	 */
	int addSuggestions(Model results, int maxCount, Function<RDFNode,String> labelFunction);

	
	/**
	 * Adds dash:suggestion triples for a given result resource.
	 * @param result  the sh:ValidationResult to add the suggestions to
	 * @param maxCount  the maximum number of suggestions to produce
	 * @param labelFunction  an optional function producing labels of nodes
	 * @return the number of suggestions that were created
	 */
	int addSuggestions(Resource result, int maxCount, Function<RDFNode,String> labelFunction);
	
	
	/**
	 * Checks if this is (in principle) capable of adding suggestions for a given result.
	 * @param result  the validation result
	 * @return true if this can
	 */
	boolean canHandle(Resource result);
}
