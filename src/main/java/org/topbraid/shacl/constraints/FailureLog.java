package org.topbraid.shacl.constraints;

import org.topbraid.shacl.vocabulary.SH;

/**
 * A singleton to record (and possibly print) failures thrown by the validation engine.
 * Can be overloaded to install different failure handling.
 * 
 * @author Holger Knublauch
 */
public class FailureLog {

	private static FailureLog singleton = new FailureLog();
	
	public static FailureLog get() {
		return singleton;
	}
	
	public static void set(FailureLog value) {
		FailureLog.singleton = value;
	}
	
	
	public void logFailure(String message) {
		System.err.println(SH.NAME + " Validation Failure: " + message);
	}
}
