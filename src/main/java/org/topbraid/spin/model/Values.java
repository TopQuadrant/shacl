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

import org.apache.jena.sparql.engine.binding.Binding;

/**
 * A VALUES element (inside of a WHERE clause).
 * 
 * @author Holger Knublauch
 */
public interface Values extends Element {
	
	/**
	 * Gets the bindings (rows), from top to bottom as entered.
	 * @return the Bindings
	 */
	List<Binding> getBindings();

	/**
	 * Gets the names of the declared variables, ordered as entered.
	 * @return the variable names
	 */
	List<String> getVarNames();
}
