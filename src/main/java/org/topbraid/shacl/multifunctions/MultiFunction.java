package org.topbraid.shacl.multifunctions;

import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.QueryIterator;

/**
 * The base interface of (DASH) multi-functions.
 * Multi-functions are procedures that take zero or more input variable bindings ("left hand side")
 * and return zero or more output variable bindings, potentially with multiple variables per binding.
 * 
 * They are similar to SPIN magic properties, yet more focused, see TBS-4117.
 * In particular they only support the case where the values on the right hand side are computed
 * from the values of the left hand side, not any other direction.
 * 
 * The MultiFunction interface also defines functions to access metadata about the multi-function,
 * for example for documentation generation purposes.
 * 
 * @author Holger Knublauch
 */
public interface MultiFunction {
	
	/**
	 * Executes the multi-function for a given list of arguments.
	 * @param args  the argument values, matching the declared parameters from left to right
	 * @param activeGraph  the currently active query graph
	 * @param dataset  the DatasetGraph to operate on
	 * @return a QueryIterator with name-value pairs
	 */
	QueryIterator execute(List<Node> args, Graph activeGraph, DatasetGraph dataset);
	
	
	/**
	 * Gets the API status.
	 * @return one of the instances of dash:APIStatus or null if this should not be used outside.
	 */
	Node getAPIStatus();
	
	
	/**
	 * Gets human-readable documentation for the MultiFunction, e.g. for code generation.
	 * @return the description or null
	 */
	String getDescription();
	
	
	/**
	 * Gets metadata about the declared parameters (based on sh:parameter).
	 * @return the parameters
	 */
	List<MultiFunctionParameter> getParameters();
	
	
	/**
	 * Gets metadata about the variables that will be produced for each solution (based on dash:resultVariable).
	 * @return the result variables
	 */
	List<MultiFunctionParameter> getResultVars();
	
	
	/**
	 * Gets the URI of this MultiFunction, which can also be used for the SPARQL property function.
	 * @return the URI string
	 */
	String getURI();
}
