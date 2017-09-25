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

import org.apache.jena.rdf.model.RDFNode;



/**
 * Jena wrapper for instances of spl:Argument.
 * 
 * @author Holger Knublauch
 */
public interface Argument extends AbstractAttribute {
	
	
	/**
	 * If this is an ordered arg (sp:arg1, sp:arg2, ...) then this returns
	 * the index of this, otherwise null.
	 * @return the arg index or null if this does not have an index
	 */
	Integer getArgIndex();
	
	
	/**
	 * Returns any declared spl:defaultValue.
	 * @return the default value or null
	 */
	RDFNode getDefaultValue();

	
	/**
	 * Gets the variable name associated with this Argument.
	 * This is the local name of the predicate, i.e. implementations
	 * can assume that this value is not null iff getPredicate() != null.
	 * @return the variable name
	 */
	String getVarName();
}
