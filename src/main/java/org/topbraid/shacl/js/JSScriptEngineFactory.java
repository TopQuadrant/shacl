package org.topbraid.shacl.js;

/**
 * A singleton that should be used to produce new JSScriptEngines.
 * Returns a Nashorn-based engine by default.
 * 
 * @author Holger Knublauch
 */
public class JSScriptEngineFactory {

	private static JSScriptEngineFactory singleton = new JSScriptEngineFactory();
	
	public static JSScriptEngineFactory get() {
		return singleton;
	}
	
	public static void set(JSScriptEngineFactory value) {
		JSScriptEngineFactory.singleton = value;
	}
	
	
	public JSScriptEngine createScriptEngine() {
		return new NashornScriptEngine();
	}
}
