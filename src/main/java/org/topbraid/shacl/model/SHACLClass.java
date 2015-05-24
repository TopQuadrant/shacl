package org.topbraid.shacl.model;


public interface SHACLClass extends SHACLResource {

	
	/**
	 * Checks if this class has been declared to be abstract using sh:abstract.
	 * @return true  if this is abstract
	 */
	boolean isAbstract();
}
