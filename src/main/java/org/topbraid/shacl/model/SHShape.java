package org.topbraid.shacl.model;

import java.util.List;

import org.apache.jena.rdf.model.RDFNode;

public interface SHShape extends SHParameterizableConstraint {

	/**
	 * Gets all sh:PropertyConstraints including sh:Parameter declarations
	 * that are about a given predicate.
	 * @param predicate  the predicate
	 * @return a possibly empty list
	 */
	List<SHPropertyConstraint> getPropertyConstraints(RDFNode predicate);
	
	
	/**
	 * Checks if this shape has been deactivated by having sh:filterShape dash:None.
	 * @return true if deactivated
	 */
	boolean isDeactivated();
}
