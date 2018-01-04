package org.topbraid.shacl.util;

import org.topbraid.shacl.arq.SHACLFunctionDriver;
import org.topbraid.shacl.validation.ConstraintExecutors;
import org.topbraid.shacl.validation.TargetPlugins;

/**
 * Manages global preferences related to SHACL processing.
 * 
 * @author Holger Knublauch
 */
public class SHACLPreferences {
	
	private static boolean jsPreferred;
	
	private static boolean produceFailuresMode;

	
	/**
	 * Checks if JavaScript is preferred over SPARQL for the execution of constraint
	 * validators, functions etc.
	 * @return true if JavaScript if preferred
	 */
	public static boolean isJSPreferred() {
		return jsPreferred;
	}
	
	
	/**
	 * Checks if any Exceptions thrown during validation shall be wrapped into dash:FailureResults.
	 * This might be useful for debugging and is default in TopBraid Composer.
	 * If false (default), Exceptions halt the engine altogether.
	 * @return true  if failures should be produced
	 */
	public static boolean isProduceFailuresMode() {
		return produceFailuresMode;
	}
	
	
	public static void setJSPreferred(boolean value) {
		jsPreferred = value;
		ConstraintExecutors.get().setJSPreferred(value);
		SHACLFunctionDriver.setJSPreferred(value);
		TargetPlugins.get().setJSPreferred(value);
	}
	
	
	public static void setProduceFailuresMode(boolean value) {
		produceFailuresMode = value;
	}
}
