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
import java.util.Map;

import org.topbraid.spin.model.print.Printable;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;


/**
 * Part of a SPARQL expression that calls a Function.
 * 
 * @author Holger Knublauch
 */
public interface FunctionCall extends Printable, ModuleCall {

	/**
	 * Gets a list of argument RDFNodes, whereby each RDFNode is already cast
	 * into the most specific subclass possible.  In particular, arguments are
	 * either instances of Variable, FunctionCall or RDFNode (constant)
	 * @return the List of arguments
	 */
	List<RDFNode> getArguments();
	
	
	/**
	 * Gets a Map from properties (such as sp:arg1, sp:arg2) to their declared
	 * argument values.  The map will only contain non-null arguments.
	 * @return a Map of arguments
	 */
	Map<Property,RDFNode> getArgumentsMap();

	
	/**
	 * Gets the URI Resource of the Function being called here.
	 * The resulting Resource will be in the function's defining
	 * Model, for example if loaded into the library from a .spin. file.
	 * @return the function in its original Model
	 */
	Resource getFunction();
}
