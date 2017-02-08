package org.topbraid.shacl.js;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.js.model.JSFactory;
import org.topbraid.shacl.js.model.TermFactory;
import org.topbraid.spin.util.ExceptionUtil;
import org.topbraid.spin.util.JenaUtil;

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
	
	private ScriptEngine engine;
	
	// Remembers which sh:libraries executables were already handled so that they are
	// not installed twice
	private Set<Resource> visitedLibraries = new HashSet<>();
	
	
	public NashornScriptEngine() {
		engine = new ScriptEngineManager().getEngineByName("nashorn");
		engine.put("TermFactory", new TermFactory());
		try {
			engine.eval(ARGS_FUNCTION);
		}
		catch(ScriptException ex) {
			ExceptionUtil.throwUnchecked(ex);
		}
	}
	
	
	@Override
	public void eval(String expr) throws ScriptException {
		engine.eval(expr);
	}


	public void executeLibraries(Resource e) throws Exception {
		for(Resource library : JenaUtil.getResourceProperties(e, SHJS.jsLibrary)) {
			if(!visitedLibraries.contains(library)) {
				visitedLibraries.add(library);
				executeLibraries(library);
			}
		}
		for(Statement s : e.listProperties(SHJS.jsLibraryURL).toList()) {
			if(s.getObject().isLiteral()) {
				String url = s.getString();
				executeScriptFromURL(url);
			}
		}
	}
	
	
	public void executeScriptFromURL(String url) throws Exception {
		Reader reader = new InputStreamReader(new URL(url).openStream());
		engine.eval(reader);
		reader.close();
	}
	
	
	@Override
	public Object get(String varName) {
		return engine.get(varName);
	}


	protected final ScriptEngine getEngine() {
		return engine;
	}
	
	
	private List<String> getFunctionParameters(String functionName) throws ScriptException {
		Object what = engine.get(functionName);
		try {
			String funcString = what.toString();
			Object result = ((Invocable) engine).invokeFunction(ARGS_FUNCTION_NAME, funcString);
			Object[] params = NashornUtil.asArray(result);
			List<String> results = new ArrayList<String>(params.length);
			for(Object param : params) {
				results.add((String)param);
			}
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
		return ((Invocable) engine).invokeFunction(functionName, params);
	}


	public void put(String varName, Object value) {
		engine.put(varName, value);
	}
}
