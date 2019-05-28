package org.topbraid.jenax.util;

/**
 * A singleton that holds a reference to an installed AutoCompleteEngine (if one exists).
 * 
 * @author Holger Knublauch
 */
public class AutoCompleteManager {
	
	private static AutoCompleteEngine engine;
	
	public static AutoCompleteEngine getEngine() {
		return engine;
	}
	
	public static void setEngine(AutoCompleteEngine engine) {
		AutoCompleteManager.engine = engine;
	}
}
