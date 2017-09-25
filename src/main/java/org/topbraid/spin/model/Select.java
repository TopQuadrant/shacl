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

import org.apache.jena.rdf.model.Resource;


/**
 * A SELECT query.
 * 
 * @author Holger Knublauch
 */
public interface Select extends SolutionModifierQuery {
	
	/**
	 * Gets the names of all variables in the SELECT.
	 * Returns an empty List for SELECT *.
	 * List entries may be null if they are expressions only. 
	 * @return a List of variable names
	 */
	List<String> getResultVariableNames();
	

	/**
	 * Gets a list of result variables, or an empty list if we have a star
	 * results list.  Note that the "variables" may in fact be
	 * wrapped aggregations or expressions.
	 * The results can be tested with instanceof against
	 * <code>Variable</code>, <code>Aggregation</code> or
	 * <code>FunctionCall</code>.  Variables can have an additional
	 * <code>sp:expression</code>, representing AS expressions.
	 * 
	 * @deprecated Note that this function returns an empty result if the query is
	 * represented using sp:text only.
	 * This means that it should not be used outside of SPIN RDF structures.
	 * Use #getResultVariableNames otherwise.
	 * This method will be made private in future versions.
	 * 
	 * @return the result "variables"
	 */
	List<Resource> getResultVariables();
	
	
	/**
	 * Checks is this query has the DISTINCT flag set.
	 * @return true if distinct
	 */
	boolean isDistinct();
	

	/**
	 * Checks if this query has the REDUCED flag set.
	 * @return true if reduced
	 */
	boolean isReduced();
}
