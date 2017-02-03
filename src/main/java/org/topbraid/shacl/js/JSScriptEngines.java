package org.topbraid.shacl.js;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.js.model.TermFactory;
import org.topbraid.shacl.model.SHJSExecutable;
import org.topbraid.spin.util.JenaUtil;

/**
 * A singleton that should be used to produce a new JavaScript engine.
 * Returns the usual Nashorn engine by default.
 * 
 * @author Holger Knublauch
 */
public class JSScriptEngines {
	
	private AtomicLong counter = new AtomicLong();

	private static JSScriptEngines singleton = new JSScriptEngines();
	
	public static JSScriptEngines get() {
		return singleton;
	}
	
	public static void set(JSScriptEngines value) {
		JSScriptEngines.singleton = value;
	}
	
	
	public ScriptEngine createScriptEngine() {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		engine.put("TermFactory", new TermFactory());
		return engine;
	}
	
	
	public String installFunction(ScriptEngine engine, String body) throws Exception {
		String functionName = "SHACLJSFunction_" + counter.getAndIncrement();
		String expr = "function " + functionName + "() {\n" + body + "\n}";
		engine.eval(expr);
		return functionName;
	}
	
	
	public void executeLibraries(ScriptEngine engine, SHJSExecutable exec, Set<Resource> visited, boolean doScript) throws Exception {
		for(Resource library : JenaUtil.getResourceProperties(exec, SHJS.library)) {
			if(!visited.contains(library)) {
				executeLibraries(engine, library.as(SHJSExecutable.class), visited, true);
			}
			for(Statement s : exec.listProperties(SHJS.scriptURL).toList()) {
				if(s.getObject().isLiteral()) {
					String url = s.getString();
					executeScriptFromURL(engine, url);
				}
			}
		}
		if(doScript) {
			String script = exec.getScript();
			if(script != null) {
				engine.eval(script);
			}
		}
	}
	
	
	protected void executeScriptFromURL(ScriptEngine engine, String url) throws Exception {
		Reader reader = new InputStreamReader(new URL(url).openStream());
		engine.eval(reader);
		reader.close();
	}
}
