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
package org.topbraid.spin.arq;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.function.FunctionFactory;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.model.Function;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SPIN;

/**
 * A helper object that can be used to register SPARQL functions
 * (and property functions) per thread, e.g. per servlet request.
 * 
 * @author Holger Knublauch
 */
public class SPINThreadFunctions {
	
	private Map<String,FunctionFactory> functionsCache = new HashMap<String,FunctionFactory>();
	
	private Map<String,PropertyFunctionFactory> pfunctionsCache = new HashMap<String,PropertyFunctionFactory>();

	private Model model;
	
	
	SPINThreadFunctions(Model model) {
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
	
	
	PropertyFunctionFactory getPFunctionFactory(String uri) {
		PropertyFunctionFactory old = pfunctionsCache.get(uri);
		if(old != null) {
			return old;
		}
		else if(pfunctionsCache.containsKey(uri)) {
			return null;
		}
		else {
			return getPropertyFunctionFactoryFromModel(uri);
		}
	}


	private FunctionFactory getFunctionFactoryFromModel(String uri) {
		Function spinFunction = model.getResource(uri).as(Function.class);
		if(JenaUtil.hasIndirectType(spinFunction, SPIN.Function)) {
			FunctionFactory arqFunction = SPINFunctionDrivers.get().create(spinFunction);
			if(arqFunction != null) {
				functionsCache.put(uri, arqFunction);
				return arqFunction;
			}
		}
		else if(JenaUtil.hasIndirectType(spinFunction, SH.Function)) {
			FunctionFactory arqFunction = SPINFunctionDrivers.get().create(spinFunction);
			if(arqFunction != null) {
				functionsCache.put(uri, arqFunction);
				return arqFunction;
			}
		}
		// Remember failed attempt for future
		functionsCache.put(uri, null);
		return null;
	}


	private PropertyFunctionFactory getPropertyFunctionFactoryFromModel(String uri) {
		Function spinFunction = model.getResource(uri).as(Function.class);
		if(JenaUtil.hasIndirectType(spinFunction, SPIN.MagicProperty)) {
			if(spinFunction.hasProperty(SPIN.body)) {
				final SPINARQPFunction arqFunction = SPINARQPFunctionFactory.get().create(spinFunction);
				pfunctionsCache.put(uri, arqFunction);
				return arqFunction;
			}
		}
		// Remember failed attempt for future
		pfunctionsCache.put(uri, null);
		return null;
	}
}
