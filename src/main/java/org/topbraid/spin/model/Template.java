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


/**
 * A template class definition.
 * 
 * @author Holger Knublauch
 */
public interface Template extends Module {

	/**
	 * Gets the declared spin:labelTemplate (if any exists).
	 * @return the label template string or null
	 */
	String getLabelTemplate();

	
	/**
	 * Gets the declared spin:labelTemplate (if any exists), using a preferred language.
	 * @param lang  the preferred language tag, e.g. "de"
	 * @return the label template string or null
	 */
	String getLabelTemplate(String lang);
}
