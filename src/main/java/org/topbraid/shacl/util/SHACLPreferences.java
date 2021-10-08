package org.topbraid.shacl.util;

/**
 * Manages global preferences related to SHACL processing.
 * 
 * @author Holger Knublauch
 */
public class SHACLPreferences {
	
	private static boolean produceFailuresMode;
	
	
	/**
	 * Checks if any Exceptions thrown during validation shall be wrapped into dash:FailureResults.
	 * This might be useful for debugging and is default in TopBraid Composer.
	 * If false (default), Exceptions halt the engine altogether.
	 * @return true  if failures should be produced
	 */
	public static boolean isProduceFailuresMode() {
		return produceFailuresMode;
	}
	
	
	public static void setProduceFailuresMode(boolean value) {
		produceFailuresMode = value;
	}
}
