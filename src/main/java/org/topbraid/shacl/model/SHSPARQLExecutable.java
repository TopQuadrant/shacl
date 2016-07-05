package org.topbraid.shacl.model;

public interface SHSPARQLExecutable extends SHResource {

	/**
	 * Gets the value of sh:sparql.
	 * @return the SPARQL query or null
	 */
	String getSPARQL();
}
