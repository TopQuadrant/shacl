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

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;


/**
 * A Resource that also may have spin constraints or rules attached to it.
 * This is basically a convenience layer that can be used to access those
 * constraints and rules more easily.
 * 
 * @author Holger Knublauch
 */
public interface SPINInstance extends Resource {

	/**
	 * Gets the queries and template calls associated with this.
	 * @param predicate  the predicate such as <code>spin:rule</code>
	 * @return a List of QueryOrTemplateCall instances
	 */
	List<QueryOrTemplateCall> getQueriesAndTemplateCalls(Property predicate);
}
