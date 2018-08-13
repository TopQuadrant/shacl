package org.topbraid.shacl.engine;

@SuppressWarnings("serial")
public class SHACLCanceledException extends RuntimeException {

	public SHACLCanceledException() {
		super("SHACL engine canceled");
	}
}
