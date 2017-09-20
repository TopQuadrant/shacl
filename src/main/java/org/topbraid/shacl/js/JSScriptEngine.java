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
package org.topbraid.shacl.js;

import javax.script.ScriptException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Resource;

/**
 * Abstraction layer between TopBraid and the (Nashorn) JavaScript API.
 * This layer allows different TopBraid products to install additional features.
 * 
 * @author Holger Knublauch
 */
public interface JSScriptEngine {
	
	Object eval(String expr) throws ScriptException;

	void executeLibraries(Resource exec) throws Exception;
	
	void executeScriptFromURL(String url) throws Exception;

	Object get(String varName);
	
	Object invokeFunction(String functionName, QuerySolution bindings) throws javax.script.ScriptException, java.lang.NoSuchMethodException;
	
	Object invokeFunctionOrdered(String functionName, Object[] args) throws javax.script.ScriptException, java.lang.NoSuchMethodException;

	void put(String varName, Object value);
}
