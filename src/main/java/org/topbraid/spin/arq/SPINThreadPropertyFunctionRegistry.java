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

import java.util.Iterator;

import org.apache.jena.sparql.pfunction.PropertyFunctionFactory;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

/**
 * An ARQ PropertyFunctionRegistry that can be used to associate functions
 * with Threads, so that additional functions from a given Model can
 * be made visible depending on the SPARQL query thread.
 * 
 * Note that this concept only works if ARQ has been set to single
 * threading, which is done by the static block below.
 * 
 * The contract of this class is very strict to prevent memory leaks:
 * Users always need to make sure that unregister is called as soon
 * as a query (block) ends.
 * 
 * @author Holger Knublauch
 */
public class SPINThreadPropertyFunctionRegistry extends PropertyFunctionRegistry {
	
	private PropertyFunctionRegistry base;
	
	
	public SPINThreadPropertyFunctionRegistry(PropertyFunctionRegistry base) {
		this.base = base;
	}


	@Override
	public PropertyFunctionFactory get(String uri) {
		PropertyFunctionFactory b = base.get(uri);
		if(b != null) {
			return b;
		}
		SPINThreadFunctions functions = SPINThreadFunctionRegistry.getFunctions();
		if(functions != null) {
			PropertyFunctionFactory ff = functions.getPFunctionFactory(uri);
			if(ff != null) {
				return ff;
			}
		}
		return null;
	}


	@Override
	public boolean isRegistered(String uri) {
		if(base.isRegistered(uri)) {
			return true;
		}
		return get(uri) != null;
	}


	@Override
	public Iterator<String> keys() {
		// Note: only includes base keys
		return base.keys();
	}


	@Override
	public boolean manages(String uri) {
		if(base.manages(uri)) {
			return true;
		}
		else {
			return get(uri) != null;
		}
	}


	@Override
	public void put(String uri, Class<?> extClass) {
		base.put(uri, extClass);
	}


	@Override
	public void put(String uri, PropertyFunctionFactory factory) {
		base.put(uri, factory);
	}


	@Override
	public PropertyFunctionFactory remove(String uri) {
		return base.remove(uri);
	}
}
