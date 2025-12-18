package org.topbraid.shacl.engine;

public class SHACLCanceledException extends RuntimeException {

	public SHACLCanceledException() {
		super("SHACL engine canceled");
	}
}
