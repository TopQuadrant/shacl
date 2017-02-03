package org.topbraid.shacl.model;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public interface SHPropertyShape extends SHShape {
	
	/**
	 * Gets the declared sh:class, sh:directType or sh:datatype (if any).
	 * If none is declared, falls back to sh:nodeKind, e.g. returning rdfs:Resource
	 * if sh:nodeKind is sh:IRI.
	 * @return the value type or data type
	 */
	Resource getClassOrDatatype();
	
	
	String getCountDisplayString();

	
	String getDescription();
	
	
	Integer getMaxCount();
	
	
	Integer getMinCount();
	
	
	/**
	 * Gets the sh:order of this
	 * @return the order or null no sh:order is given
	 */
	Integer getOrder();
	
	
	Resource getPath();


	/**
	 * Gets the property represented by the sh:path, assuming it's a IRI.
	 * Returns null if it's a property path (blank node).
	 * @return the predicate or null
	 */
	Property getPredicate();

	
	/**
	 * Gets the variable name associated with this.
	 * This is the local name of the predicate, i.e. implementations
	 * can assume that this value is not null iff getPredicate() != null.
	 * @return the variable name
	 */
	String getVarName();
}
