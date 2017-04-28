package org.topbraid.shacl.validation;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.validation.js.JSTargetPlugin;
import org.topbraid.shacl.validation.sparql.SPARQLTargetPlugin;

/**
 * A singleton managing the available custom target plugins.
 * The only currently supported custom target is for SPARQL.
 * 
 * @author Holger Knublauch
 */
public class TargetPlugins {

	private static TargetPlugins singleton = new TargetPlugins();
	
	public static TargetPlugins get() {
		return singleton;
	}
	
	
	private final List<TargetPlugin> plugins = new LinkedList<>();
	
	TargetPlugins() {
		addPlugin(new SPARQLTargetPlugin());
		addPlugin(new JSTargetPlugin());
	}
	
	
	public void addPlugin(TargetPlugin plugin) {
		plugins.add(plugin);
	}
	
	
	public TargetPlugin getLanguageForTarget(Resource target) {
		for(TargetPlugin plugin : plugins) {
			if(plugin.canExecuteTarget(target)) {
				return plugin;
			}
		}
		return null;
	}
}
