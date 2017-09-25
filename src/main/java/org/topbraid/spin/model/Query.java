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


/**
 * Base interface of the various SPARQL query types such as
 * Ask, Construct, Describe and Select.
 * 
 * @author Holger Knublauch
 */
public interface Query extends CommandWithWhere {
	
	/**
	 * Gets the list of URIs specified in FROM clauses.
	 * @return a List of URI Strings
	 */
	List<String> getFrom();
	
	
	/**
	 * Gets the list of URIs specified in FROM NAMED clauses.
	 * @return a List of URI Strings
	 */
	List<String> getFromNamed();
	
	
	/**
	 * Gets the VALUES block at the end of the query if it exists. 
	 * @return the Values or null
	 */
	Values getValues();

	
	/**
	 * Gets the elements in the WHERE clause of this query.
	 * The Elements will be typecast into the best suitable subclass.
	 * @return a List of Elements
	 */
	List<Element> getWhereElements();
}
