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
 * A SPARQL FILTER element.
 * 
 * @author Holger Knublauch
 */
public interface Filter extends Element {

	/**
	 * Gets the expression representing the filter condition.
	 * The result object will be typecast into the most specific
	 * subclass of RDFNode, e.g. FunctionCall or Variable.
	 * @return the expression or null
	 */
	RDFNode getExpression();
}
