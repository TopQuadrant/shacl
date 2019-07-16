package org.topbraid.shacl.util;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.topbraid.shacl.vocabulary.DASH;

/**
 * Manages the current execution platform, used by dash:ExecutionPlatform etc.
 * Used for example to determine if a given SPARQL constraint validator can be
 * executed or not.
 * 
 * By default this does not return true for any request, but implementations
 * such as TopBraid will install their own ExecutionPlatform instance.
 * 
 * @author Holger Knublauch
 */
public class ExecutionPlatform {

	private static ExecutionPlatform singleton = new ExecutionPlatform();
	
	public static ExecutionPlatform get() {
		return singleton;
	}
	
	public static void set(ExecutionPlatform value) {
		singleton = value;
	}
	
	
	public static boolean canExecute(Resource executable) {
		StmtIterator it = executable.listProperties(DASH.requiredExecutionPlatform);
		if(!it.hasNext()) {
			return true;
		}
		else {
			while(it.hasNext()) {
				Statement s = it.next();
				if(s.getObject().isResource() && isCompatibleWith(s.getResource())) {
					it.close();
					return true;
				}
			}
			return false;
		}
	}
	
	
	public static boolean isCompatibleWith(Resource platform) {
		if(get().isCompatibleWithExactly(platform)) {
			return true;
		}
		// Warning: this does assume that no loops exist
		for(Resource include : platform.getModel().listSubjectsWithProperty(DASH.includedExecutionPlatform, platform).toList()) {
			if(isCompatibleWith(include)) {
				return true;
			}
		}
		return false;
	}
	
	
	public boolean isCompatibleWithExactly(Resource platform) {
		return false;
	}
}
