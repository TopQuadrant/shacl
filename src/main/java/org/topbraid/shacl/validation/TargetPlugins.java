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
		init();
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
	
	
	private void init() {
		addPlugin(new SPARQLTargetPlugin());
		addPlugin(new JSTargetPlugin());
	}
	
	
	public void setJSPreferred(boolean value) {
		plugins.clear();
		if(value) {
			addPlugin(new JSTargetPlugin());
			addPlugin(new SPARQLTargetPlugin());
		}
		else {
			init();
		}
	}
}
