package org.topbraid.shacl.model;

public interface SHValidationReport extends SHResource {

	/**
	 * Checks if this Validation Report is conformant (sh:conforms)
	 * @return true if conformant
	 */
	boolean isConformant();
	
	/**
	 * Returns the list of results contained in this Validation Report (sh:result)
	 * @return
	 */
	Iterable<SHResult> getResults();
	
}
