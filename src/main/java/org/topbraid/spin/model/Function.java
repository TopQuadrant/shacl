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

import org.apache.jena.rdf.model.Resource;


/**
 * A SPIN Function module (not: FunctionCall).
 * 
 * @author Holger Knublauch
 */
public interface Function extends Module {
	
	/**
	 * Gets the value of the spin:returnType property, if any.
	 * @return the return type or null
	 */
	Resource getReturnType();
	
	
	/**
	 * Checks if this function is a magic property, marked by having
	 * rdf:type spin:MagicProperty.
	 * @return true  if this is a magic property
	 */
	boolean isMagicProperty();
	
	
	/**
	 * Indicates if spin:private is set to true for this function.
	 * @return true  if marked private
	 */
	boolean isPrivate();
}
