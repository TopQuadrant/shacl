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

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;


/**
 * Shared base class for Argument and Attribute.
 * 
 * @author Holger Knublauch
 */
public abstract interface AbstractAttribute extends Resource {
	
	/**
	 * Gets the description (stored in rdfs:comment) of this.
	 * @return the description (if any exists)
	 */
	String getComment();

	
	/**
	 * Gets the specified sp:argProperty (if any).
	 * @return the argProperty
	 */
	Property getPredicate();
	

	/**
	 * Gets the specified spl:valueType (if any).
	 * @return the value type
	 */
	Resource getValueType();
	
	
	/**
	 * Checks if this argument has been declared to be optional.
	 * For Arguments this means spl:optional = true.
	 * For Attributes this means spl:minCardinality = 0
	 * @return  true if optional
	 */
	boolean isOptional();
}
