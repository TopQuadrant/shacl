package org.topbraid.shacl.model;

import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Property;

public interface SHParameterizable extends SHResource {
	
	/**
	 * Gets an unordered List of all declared Parameters.
	 * @return the (possibly empty) List of Parameters
	 */
	List<SHParameter> getParameters();
	

	/**
	 * Gets a Map of variable names to Parameters.
	 * @return a Map of variable names to Parameters
	 */
	Map<String,SHParameter> getParametersMap();

	
	String getLabelTemplate();

	
	/**
	 * Gets an ordered List of all declared Parameters, based on
	 * the local names of the predicates and sh:index values.
	 * @return the (possibly empty) List of Parameters
	 */
	List<SHParameter> getOrderedParameters();


	/**
	 * Checks if one of the sh:parameters declaring a given predicate is also marked
	 * as sh:optional true.
	 * @param predicate  the predicate to check
	 * @return true if there is an optional declaration for the given predicate
	 */
	boolean isOptionalParameter(Property predicate);
}
