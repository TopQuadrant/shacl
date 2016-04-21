package org.topbraid.shacl.model;

public interface SHACLSPARQLExecutable extends SHACLResource {

	/**
	 * Gets the value of sh:sparql.
	 * @return the SPARQL query or null
	 */
	String getSPARQL();
}
