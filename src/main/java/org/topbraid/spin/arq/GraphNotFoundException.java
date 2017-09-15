package org.topbraid.spin.arq;

/**
 * An Exception thrown if a named graph could not be resolved
 * while setting the default graph of a dataset.
 * 
 * This is subclassing RuntimeException because otherwise a lot of
 * existing code would have to catch GraphNotFoundException
 * (where it would otherwise have crashed with a NullPointerException anyway).
 * 
 * @author Holger Knublauch
 */
public class GraphNotFoundException extends RuntimeException {

	public GraphNotFoundException(String message) {
		super(message);
	}
}
