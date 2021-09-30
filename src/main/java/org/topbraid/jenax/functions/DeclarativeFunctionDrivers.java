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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.arq.functions.SHACLSPARQLFunctionDriver;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;


/**
 * The singleton that creates ARQ FunctionFactories from (SHACL, SPIN) function declarations.
 * Can be used by applications to install a different singleton with support
 * for different kinds of functions, such as SHACL-JS or Script-based functions.
 * 
 * @author Holger Knublauch
 */
public class DeclarativeFunctionDrivers implements DeclarativeFunctionDriver {
	
	public final static Property SPIN_ABSTRACT = ResourceFactory.createProperty("http://spinrdf.org/spin#abstract");

	private static DeclarativeFunctionDrivers singleton = new DeclarativeFunctionDrivers();
	
	public static DeclarativeFunctionDrivers get() {
		return singleton;
	}
	
	public static void set(DeclarativeFunctionDrivers value) {
		singleton = value;
	}
	
	
	private Map<Property,DeclarativeFunctionDriver> drivers = new HashMap<>();
	
	DeclarativeFunctionDrivers() {
		register(SH.ask, new SHACLSPARQLFunctionDriver());
		register(SH.select, new SHACLSPARQLFunctionDriver());
	}


	@Override
	public DeclarativeFunctionFactory create(Resource function) {
		DeclarativeFunctionDriver driver = getDriver(function);
		if(driver != null) {
			return driver.create(function);
		}
		else {
			return null;
		}
	}
	

	/**
	 * Registers a new DeclarativeFunctionDriver for a given key predicate.
	 * For example, SPARQLMotion functions are recognized via sm:body.
	 * Any previous entry will be overwritten.
	 * @param predicate  the key predicate
	 * @param driver  the driver to register
	 */
	public void register(Property predicate, DeclarativeFunctionDriver driver) {
		drivers.put(predicate, driver);
	}
	
	
	private DeclarativeFunctionDriver getDriver(Resource functionR) {
		JenaUtil.setGraphReadOptimization(true);
		try {
			DeclarativeFunctionDriver direct = getDirectDriver(functionR);
			if(direct != null) {
				return direct;
			}
			else {
				return getDriver(functionR, new HashSet<Resource>());
			}
		}
		finally {
			JenaUtil.setGraphReadOptimization(false);
		}
	}
	
	
	private DeclarativeFunctionDriver getDriver(Resource functionR, Set<Resource> reached) {
		reached.add(functionR);
		for(Resource superClass : JenaUtil.getSuperClasses(functionR)) {
			if(!reached.contains(functionR)) {
				DeclarativeFunctionDriver superFunction = getDirectDriver(superClass);
				if(superFunction != null) {
					return superFunction;
				}
			}
		}
		return null;
	}
	
	
	private DeclarativeFunctionDriver getDirectDriver(Resource spinFunction) {
		if(!spinFunction.hasProperty(SPIN_ABSTRACT, JenaDatatypes.TRUE) &&
			!spinFunction.hasProperty(DASH.abstract_, JenaDatatypes.TRUE)) {
			StmtIterator it = spinFunction.listProperties();
			while(it.hasNext()) {
				Statement s = it.next();
				final DeclarativeFunctionDriver driver = drivers.get(s.getPredicate());
				if(driver != null) {
					it.close();
					return driver;
				}
			}
		}
		return null;
	}
}
