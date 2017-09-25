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

package org.topbraid.spin.model;

import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Resource;


/**
 * Instances of spin:Module (or subclasses thereof).
 * 
 * @author Holger Knublauch
 */
public interface Module extends Resource {
	
	/**
	 * Gets a List of all declared Arguments.
	 * If ordered, then the local names of the predicates are used.
	 * @param ordered  true to get an ordered list back (slower)
	 * @return the (possibly empty) List of Arguments
	 */
	List<Argument> getArguments(boolean ordered);
	

	/**
	 * Gets a Map of variable names to Arguments.
	 * @return a Map of variable names to Arguments
	 */
	Map<String,Argument> getArgumentsMap();
	
	
	/**
	 * Gets the body (if defined).  The result will be type cast into the
	 * most specific subclass of Command if possible.
	 * @return the body or null
	 */
	Command getBody();

	
	/**
	 * Gets the rdfs:comment of this (if any).
	 * @return the comment or null
	 */
	String getComment();

	
	/**
	 * Checks if this Module has been declared to be abstract using spin:abstract.
	 * @return true  if this is abstract
	 */
	boolean isAbstract();
}
