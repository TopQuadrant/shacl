package org.topbraid.shacl.constraints;

/**
 * An Exception signaling invalid input to the constraint validation engine.
 *  
 * @author Holger Knublauch
 */
@SuppressWarnings("serial")
public class SHACLException extends RuntimeException {

	public SHACLException(String message) {
		super(message);
	}
}
