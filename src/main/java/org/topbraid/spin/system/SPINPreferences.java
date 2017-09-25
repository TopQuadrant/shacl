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

package org.topbraid.spin.system;


/**
 * A singleton that provides access to the current SPIN rendering settings.
 * The singleton can be replaced to install different default settings.
 * For example, TopBraid Composer stores these settings in the Eclipse
 * preferences.
 * 
 * @author Holger Knublauch
 */
public class SPINPreferences {

	private static SPINPreferences singleton = new SPINPreferences();
	

	/**
	 * Gets the singleton instance of this class.
	 * @return the singleton
	 */
	public static SPINPreferences get() {
		return singleton;
	}
	
	
	/**
	 * Changes the singleton to some subclass.
	 * @param value  the new singleton (not null)
	 */
	public static void set(SPINPreferences value) {
		SPINPreferences.singleton = value;
	}
	

	/**
	 * Indicates whether the SPIN generator shall convert variables into
	 * URI nodes, so that they can be shared between multiple queries.
	 * @return true  to create shared URI variables (default: false)
	 */
	public boolean isCreateURIVariables() {
		return false;
	}
	
	
	/**
	 * Indicates whether the SPIN generator shall reuse the same blank node
	 * for a variable multiple times within the same query.
	 * This is off by default to make bnode structures more self-contained.
	 * @return true  to reuse blank nodes
	 */
	public boolean isReuseLocalVariables() {
		return false;
	}
}
