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

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public interface SHPropertyShape extends SHShape {
	
	/**
	 * Gets the declared sh:class or sh:datatype (if any).
	 * If none is declared, falls back to sh:nodeKind, e.g. returning rdfs:Resource
	 * if sh:nodeKind is sh:IRI.
	 * @return the value type or data type
	 */
	Resource getClassOrDatatype();
	
	
	String getCountDisplayString();


	/**
	 * Gets the sh:description, if it exists.
	 * @return the description or null
	 */
	String getDescription();
	
	
	Integer getMaxCount();
	
	
	Integer getMinCount();


	/**
	 * Gets the sh:name, if it exists.
	 * @return the name or null
	 */
	String getName();
	
	
	/**
	 * Gets the sh:order of this
	 * @return the order or null no sh:order is given
	 */
	Integer getOrder();


	/**
	 * Gets the property represented by the sh:path, assuming it's a IRI.
	 * Returns null if it's a property path (blank node).
	 * @return the predicate or null
	 */
	Property getPredicate();

	
	/**
	 * Gets the variable name associated with this.
	 * This is the local name of the predicate, i.e. implementations
	 * can assume that this value is not null iff getPredicate() != null.
	 * @return the variable name
	 */
	String getVarName();
}
