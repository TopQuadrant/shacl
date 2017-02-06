package org.topbraid.shacl.js;

/**
 * A singleton that uses a ThreadLocal to manage the life cycle of a JSScriptEngine
 * that can be shared for all JavaScript evaluated as part of a SHACL validation.
 * This prevents cases in which new objects and their JS libraries would need to be
 * created over and over again.
 * 
 * It is the responsibility of the caller to make sure that, once the validation has
 * completed, the engine is uninstalled.
 * 
 * @author Holger Knublauch
 */
public class SHACLScriptEngineManager {
	
	private static ThreadLocal<Boolean> actives = new ThreadLocal<>();

	private static ThreadLocal<JSScriptEngine> engines = new ThreadLocal<>();
	
	
	public static boolean begin() {
		if(actives.get() != null) {
			return actives.get();
		}
		else {
			actives.set(true);
			return false; // Signal that we are not nested inside of another begin/end block
		}
	}
	
	
	public static JSScriptEngine getCurrentEngine() {
		JSScriptEngine engine = engines.get();
		if(engine == null) {
			engine = JSScriptEngineFactory.get().createScriptEngine();
			engines.set(engine);
		}
		return engine;
	}
	
	
	public static void end(boolean nested) {
		if(!nested) {
			engines.remove();
			actives.remove();
		}
	}
}
