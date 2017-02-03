package org.topbraid.shacl.model;

import org.apache.jena.rdf.model.Resource;

public interface SHShape extends SHConstraint {

	/**
	 * Returns either sh:NodeShape or sh:PropertyShape.
	 * @return the context
	 */
	Resource getContext();
}
