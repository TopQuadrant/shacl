package org.topbraid.shacl.model;

import com.hp.hpl.jena.rdf.model.Resource;


public interface SHACLPropertyConstraint extends SHACLAbstractPropertyConstraint {
	
	/**
	 * Gets the declared sh:defaultValueType.
	 * @return the default type or null
	 */
	Resource getDefaultValueType();
	
	
	Integer getMaxCount();
	
	
	Integer getMinCount();
}
