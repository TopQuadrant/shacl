package org.topbraid.shacl.model;

import java.util.List;

import org.apache.jena.rdf.model.RDFNode;

public interface SHNodeShape extends SHShape {

	/**
	 * Gets all sh:PropertyConstraints including sh:Parameter declarations
	 * that are about a given predicate.
	 * @param predicate  the predicate
	 * @return a possibly empty list
	 */
	List<SHPropertyShape> getPropertyConstraints(RDFNode predicate);
}
