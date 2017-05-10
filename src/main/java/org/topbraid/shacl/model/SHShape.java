package org.topbraid.shacl.model;

import org.apache.jena.rdf.model.Resource;

public interface SHShape extends SHResource {

	/**
	 * Returns either sh:NodeShape or sh:PropertyShape.
	 * @return the context
	 */
	Resource getContext();
	
	
	/**
	 * Gets the value resource of sh:path or null (for node shapes).
	 * @return the path resource
	 */
	Resource getPath();
	
	
	/**
	 * Gets the rules attached to this shape via sh:rule.
	 * @return the rules
	 */
	Iterable<SHRule> getRules();
	
	
	/**
	 * Returns the sh:severity of this shape, defaulting to sh:Violation.
	 * @return
	 */
	Resource getSeverity();
	
	/**
	 * Checks if this shape has been deactivated.
	 * @return true if deactivated
	 */
	boolean isDeactivated();
	
	
	/**
	 * Checks if this is a property shape, based on the presence or absence of sh:path.
	 * @return true  iff this has a value for sh:path
	 */
	boolean isPropertyShape();
}
