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

import org.apache.jena.graph.Graph;


/**
 * An interface that can be implemented by a Dataset to support the
 * SWP function ui:graphWithImports.
 * 
 * @author Holger Knublauch
 */
public interface ImportsExpander {
	
	/**
	 * Starting with a given base Graph (and its URI), this method creates a new
	 * Graph that also includes the owl:imports of the base Graph.  Typically
	 * this will return a Jena MultiUnion.
	 * @param baseURI  the base URI of the base Graph
	 * @param baseGraph  the base Graph
	 * @return the Graph with imports
	 */
	Graph expandImports(String baseURI, Graph baseGraph);
}
