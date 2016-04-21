package org.topbraid.shacl.model;

import java.util.List;
import java.util.Map;

public interface SHACLParameterizable extends SHACLResource {
	
	/**
	 * Gets an unordered List of all declared Parameters.
	 * @return the (possibly empty) List of Parameters
	 */
	List<SHACLParameter> getParameters();
	

	/**
	 * Gets a Map of variable names to Parameters.
	 * @return a Map of variable names to Parameters
	 */
	Map<String,SHACLParameter> getParametersMap();

	
	String getLabelTemplate();

	
	/**
	 * Gets an ordered List of all declared Parameters, based on
	 * the local names of the predicates and sh:index values.
	 * @return the (possibly empty) List of Parameters
	 */
	List<SHACLParameter> getOrderedParameters();
}
