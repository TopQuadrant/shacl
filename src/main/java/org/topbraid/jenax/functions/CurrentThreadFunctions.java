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
package org.topbraid.jenax.functions;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.function.FunctionFactory;

/**
 * A helper object that can be used to register SPARQL functions
 * per thread, e.g. per servlet request.
 * 
 * @author Holger Knublauch
 */
public class CurrentThreadFunctions {
	
	private Map<String,FunctionFactory> functionsCache = new HashMap<>();

	private Model model;
	
	
	CurrentThreadFunctions(Model model) {
		this.model = model;
	}
	
	
	FunctionFactory getFunctionFactory(String uri) {
		FunctionFactory old = functionsCache.get(uri);
		if(old != null) {
			return old;
		}
		else if(functionsCache.containsKey(uri)) {
			return null;
		}
		else {
			return getFunctionFactoryFromModel(uri);
		}
	}


	private FunctionFactory getFunctionFactoryFromModel(String uri) {
		Resource functionResource = model.getResource(uri);
		DeclarativeFunctionDrivers drivers = DeclarativeFunctionDrivers.get();
		if (drivers == null) {
			return null;
		}
		FunctionFactory arqFunction = drivers.create(functionResource);
		if(arqFunction != null) {
			functionsCache.put(uri, arqFunction);
			return arqFunction;
		}
		else {
			// Remember failed attempt for future
			functionsCache.put(uri, null);
			return null;
		}
	}
}
