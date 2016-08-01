package org.topbraid.shacl.model;

import org.apache.jena.rdf.model.Resource;

public interface SHParameterizableConstraint extends SHConstraint {

	/**
	 * Returns either sh:Shape or sh:PropertyConstraint.
	 * @return the context
	 */
	Resource getContext();
}
