package org.topbraid.shacl.model;

import com.hp.hpl.jena.rdf.model.RDFNode;


public interface SHACLArgument extends SHACLAbstractPropertyConstraint {
	
	/**
	 * Returns any declared sh:defaultValue.
	 * @return the default value or null
	 */
	RDFNode getDefaultValue();
	
	
	/**
	 * Gets the sh:index of this argument, falling back to 0 for sh:arg1, 1 for sh:arg2 etc.
	 * @return the index or null if this doesn't have a sh:argX predicate and no sh:index is given
	 */
	Integer getIndex();
	
	
	boolean isOptional();
	
	
	boolean isOptionalAtTemplate(SHACLTemplate template);
}
