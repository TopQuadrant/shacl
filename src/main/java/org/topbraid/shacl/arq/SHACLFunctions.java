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
package org.topbraid.shacl.arq;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.function.FunctionFactory;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.topbraid.jenax.functions.DeclarativeFunctionDrivers;
import org.topbraid.jenax.functions.DeclarativeFunctionFactory;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.arq.functions.SHACLSPARQLARQFunction;
import org.topbraid.shacl.model.SHConstraintComponent;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.multifunctions.MultiFunctions;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Manages globally registered SHACL functions, usually loaded from .api.* files.
 * 
 * @author Holger Knublauch
 */
public class SHACLFunctions {
	
	private static Model globalFunctions;
	
	
	public static Model getGlobalFunctions() {
		return globalFunctions;
	}

	
	/**
	 * Registers a single SHACL function declared as a sh:Function.
	 * @param resource  the function resource
	 */
	public static void registerFunction(Resource resource) {
		FunctionFactory arqFunction = DeclarativeFunctionDrivers.get().create(resource);
		if(arqFunction != null) {
			FunctionFactory oldFF = FunctionRegistry.get().get(resource.getURI());
			if(oldFF == null || oldFF instanceof DeclarativeFunctionFactory) {
				FunctionRegistry.get().put(resource.getURI(), arqFunction);
			}
		}
	}

	
	/**
	 * Registers all SHACL functions from a given Model.
	 * @param model  the Model to register the functions from
	 */
	public static void registerFunctions(Model model) {
		
		SHFactory.ensureInited();

		Resource shaclFunctionClass = SH.Function.inModel(model);
		for(Resource resource : JenaUtil.getAllInstances(shaclFunctionClass)) {
			if(resource.isURIResource()) {
				registerFunction(resource);
			}
		}
		
		Resource ccClass = SH.ConstraintComponent.inModel(model);
		for(Resource resource : JenaUtil.getAllInstances(ccClass)) {
			perhapsRegisterFunction(resource.as(SHConstraintComponent.class), SH.validator);
		}
		
		MultiFunctions.registerAll(model);
	}
	
	
	public static void registerGlobalFunctions(Model model) {
		SHACLFunctions.globalFunctions = model;
		registerFunctions(model);
	}
	
	
	private static void perhapsRegisterFunction(SHConstraintComponent component, Property predicate) {
		for(Resource validator : JenaUtil.getResourceProperties(component, predicate)) {
			if(validator.isURIResource() && 
					!FunctionRegistry.get().isRegistered(validator.getURI()) &&
					JenaUtil.hasIndirectType(validator, SH.SPARQLAskValidator)) {
				FunctionFactory arqFunction = new SHACLSPARQLARQFunction(component, validator);
				if(arqFunction != null) {
					FunctionRegistry.get().put(validator.getURI(), arqFunction);
				}
			}
		}
	}
}
