/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.model;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;


/**
 * A SELECT query.
 * 
 * @author Holger Knublauch
 */
public interface Select extends SolutionModifierQuery {
	
	/**
	 * Gets the names of all variables in the SELECT.
	 * Returns an empty List for SELECT *.
	 * List entries may be null if they are expressions only. 
	 * @return a List of variable names
	 */
	List<String> getResultVariableNames();
	

	/**
	 * Gets a list of result variables, or an empty list if we have a star
	 * results list.  Note that the "variables" may in fact be
	 * wrapped aggregations or expressions.
	 * The results can be tested with instanceof against
	 * <code>Variable</code>, <code>Aggregation</code> or
	 * <code>FunctionCall</code>.  Variables can have an additional
	 * <code>sp:expression</code>, representing AS expressions.
	 * 
	 * @deprecated Note that this function returns an empty result if the query is
	 * represented using sp:text only.
	 * This means that it should not be used outside of SPIN RDF structures.
	 * Use #getResultVariableNames otherwise.
	 * This method will be made private in future versions.
	 * 
	 * @return the result "variables"
	 */
	List<Resource> getResultVariables();
	
	
	/**
	 * Checks is this query has the DISTINCT flag set.
	 * @return true if distinct
	 */
	boolean isDistinct();
	

	/**
	 * Checks if this query has the REDUCED flag set.
	 * @return true if reduced
	 */
	boolean isReduced();
}
