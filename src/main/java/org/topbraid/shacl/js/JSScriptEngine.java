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
	
	void eval(String expr) throws ScriptException;

	void executeLibraries(Resource exec) throws Exception;
	
	void executeScriptFromURL(String url) throws Exception;

	Object get(String varName);
	
	Object invokeFunction(String functionName, QuerySolution bindings) throws javax.script.ScriptException, java.lang.NoSuchMethodException;
	
	Object invokeFunctionOrdered(String functionName, Object[] args) throws javax.script.ScriptException, java.lang.NoSuchMethodException;

	void put(String varName, Object value);
}
