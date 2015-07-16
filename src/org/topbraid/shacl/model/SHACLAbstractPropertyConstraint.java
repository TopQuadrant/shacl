package org.topbraid.shacl.model;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public interface SHACLAbstractPropertyConstraint extends SHACLTemplateCall {
	
	String getComment();
	

	Property getPredicate();
	
	
	/**
	 * Gets the declared sh:valueClass, sh:directValueType or sh:datatype (if any).
	 * @return the value type or data type
	 */
	Resource getValueTypeOrDatatype();

	
	/**
	 * Gets the variable name associated with this.
	 * This is the local name of the predicate, i.e. implementations
	 * can assume that this value is not null iff getPredicate() != null.
	 * @return the variable name
	 */
	String getVarName();
}
