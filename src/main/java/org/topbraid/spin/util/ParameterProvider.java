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

package org.topbraid.spin.util;

import java.util.Iterator;

/**
 * A platform independent wrapper of HttpServletRequest, factoring out the <CODE>getParameter</CODE>
 * method.
 *
 * @author Holger Knublauch
 */
public interface ParameterProvider {

	/**
	 * Gets the value of a given parameter.
	 * The value "" is a real value, and does not indicate 'not defined'.
	 * i.e. if the parameter is missing, this must return null.
	 * @param key  the parameter
	 * @return the value or null if the parameter is not defined.
	 */
	String getParameter(String key);


	/**
	 * Gets an Iterator over all known parameter names.
	 * @return the names
	 */
	Iterator<String> listParameterNames();
}
