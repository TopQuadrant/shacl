package org.topbraid.shacl.model;

import com.hp.hpl.jena.rdf.model.RDFNode;


public interface SHACLArgument extends SHACLAbstractPropertyConstraint {
	
	/**
	 * Returns any declared sh:defaultValue.
	 * @return the default value or null
	 */
	RDFNode getDefaultValue();
	
	
	boolean isOptional();
	
	
	boolean isOptionalAtTemplate(SHACLTemplate template);
}
