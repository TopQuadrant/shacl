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

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.jenax.util.ExceptionUtil;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.js.model.JSFactory;
import org.topbraid.shacl.js.model.TermFactory;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Default implementation of JSScriptEngine, based on Nashorn.
 * 
 * @author Holger Knublauch
 */
public class NashornScriptEngine implements JSScriptEngine {
	
	private final static String ARGS_FUNCTION_NAME = "theGoodOldArgsFunction";
	
	// Copied from https://davidwalsh.name/javascript-arguments
	private final static String ARGS_FUNCTION =
			"function " + ARGS_FUNCTION_NAME + "(funcString) {\n" +
			"    var args = funcString.match(/function\\s.*?\\(([^)]*)\\)/)[1];\n" +
			"    return args.split(',').map(function(arg) {\n" +
		    "        return arg.replace(/\\/\\*.*\\*\\//, '').trim();\n" +
		  	"    }).filter(function(arg) {\n" +
		    "        return arg;\n" +
			"    });\n" +
			"}";
	
	public static final String DASH_JS = "http://datashapes.org/js/dash.js";

	public static final String RDFQUERY_JS = "http://datashapes.org/js/rdfquery.js";

	private ScriptEngine engine;
	
	private Map<String,List<String>> functionParametersMap = new HashMap<>();
	
	// Remembers which sh:libraries executables were already handled so that they are
	// not installed twice
	private Set<Resource> visitedLibraries = new HashSet<>();
	
	private Set<String> loadedURLs = new HashSet<>();
	
	
	public NashornScriptEngine() {
		engine = findNashorn();
		engine.put("TermFactory", new TermFactory());
		try {
			engine.eval(ARGS_FUNCTION);
		}
		catch(ScriptException ex) {
			ExceptionUtil.throwUnchecked(ex);
		}
	}

	private ScriptEngine findNashorn() {
		ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
		if (nashorn == null) {
			nashorn = new ScriptEngineManager(null).getEngineByName("nashorn");
		}
		if (nashorn == null) {
			throw new RuntimeException("Oracle Nashorn not found in the current context");
		}
		return nashorn;
	}
	
	
	@Override
	public Object eval(String expr) throws ScriptException {
		return engine.eval(expr);
	}


	@Override
    public void executeLibraries(Resource e) throws Exception {
		for(Resource library : JenaUtil.getResourceProperties(e, SH.jsLibrary)) {
			if(!visitedLibraries.contains(library)) {
				visitedLibraries.add(library);
				executeLibraries(library);
			}
		}
		for(Statement s : e.listProperties(SH.jsLibraryURL).toList()) {
			if(s.getObject().isLiteral()) {
				String url = s.getString();
				executeScriptFromURL(url);
			}
		}
	}
	
	
	@Override
    public final void executeScriptFromURL(String url) throws Exception {
		if(!loadedURLs.contains(url)) {
			loadedURLs.add(url);
			try ( Reader reader = createScriptReader(url) ) {
			    engine.eval(reader);
			}
		}
	}


	protected Reader createScriptReader(String url) throws Exception {
		if(DASH_JS.equals(url)) {
			return new InputStreamReader(NashornScriptEngine.class.getResourceAsStream("/js/dash.js"));
		}
		else if(RDFQUERY_JS.equals(url)) {
			return new InputStreamReader(NashornScriptEngine.class.getResourceAsStream("/js/rdfquery.js"));
		}
		else {
			return new InputStreamReader(new URL(url).openStream());
		}
	}
	
	
	@Override
	public Object get(String varName) {
		return engine.get(varName);
	}


	public final ScriptEngine getEngine() {
		return engine;
	}
	
	
	private List<String> getFunctionParameters(String functionName) throws ScriptException {
		List<String> cached = functionParametersMap.get(functionName);
		if(cached != null) {
			return cached;
		}
		Object what = engine.get(functionName);
		if(what == null) {
			throw new ScriptException("Cannot find JavaScript function \"" + functionName + "\"");
		}
		try {
			String funcString = what.toString();
			Object result = ((Invocable) engine).invokeFunction(ARGS_FUNCTION_NAME, funcString);
			Object[] params = NashornUtil.asArray(result);
			List<String> results = new ArrayList<String>(params.length);
			for(Object param : params) {
				results.add((String)param);
			}
			functionParametersMap.put(functionName, results);
			return results;
		}
		catch(Exception ex) {
			throw new ScriptException(ex);
		}
	}

	
	@Override
	public Object invokeFunction(String functionName, QuerySolution bindings) throws javax.script.ScriptException, java.lang.NoSuchMethodException {
		List<String> functionParams = getFunctionParameters(functionName);
		Object[] params = new Object[functionParams.size()];
		Iterator<String> varNames = bindings.varNames();
		while(varNames.hasNext()) {
			String varName = varNames.next();
			int index = functionParams.indexOf(varName);
			if(index < 0) {
				index = functionParams.indexOf("$" + varName);
			}
			if(index >= 0) {
				RDFNode value = bindings.get(varName);
				if(value != null) {
					params[index] = JSFactory.asJSTerm(value.asNode());
				}
			}
		}
		return invokeFunctionOrdered(functionName, params);
	}


	@Override
	public Object invokeFunctionOrdered(String functionName, Object[] params)
			throws ScriptException, NoSuchMethodException {
		return ((Invocable) engine).invokeFunction(functionName, params);
	}


	@Override
    public void put(String varName, Object value) {
		engine.put(varName, value);
	}
}
