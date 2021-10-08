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
package org.topbraid.shacl.model;

import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Property;

public interface SHParameterizable extends SHResource {
	
	/**
	 * Gets an unordered List of all declared Parameters.
	 * @return the (possibly empty) List of Parameters
	 */
	List<SHParameter> getParameters();
	

	/**
	 * Gets a Map of variable names to Parameters.
	 * @return a Map of variable names to Parameters
	 */
	Map<String,SHParameter> getParametersMap();

	
	/**
	 * Gets the sh:labelTemplate if that exists.
	 * @return the label template string or null
	 */
	String getLabelTemplate();

	
	/**
	 * Gets an ordered List of all declared SHParameters, based on
	 * sh:order values (if one of them exists), then the local names of the path predicates.
	 * @return the (possibly empty) List of SHParameters
	 */
	List<SHParameter> getOrderedParameters();


	/**
	 * Checks if one of the sh:parameters declaring a given predicate is also marked
	 * as sh:optional true.
	 * @param predicate  the predicate to check
	 * @return true if there is an optional declaration for the given predicate
	 */
	boolean isOptionalParameter(Property predicate);
}
