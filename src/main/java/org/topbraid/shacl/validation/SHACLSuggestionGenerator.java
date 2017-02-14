package org.topbraid.shacl.validation;

import java.util.function.Function;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * An interface for objects that can produce suggestions for a given results graph.
 * 
 * @author Holger Knublauch
 */
public interface SHACLSuggestionGenerator {
	
	/**
	 * Adds dash:suggestion triples for all result resource in the given results Model.
	 * @param results  the results Model
	 * @param maxCount  the maximum number of suggestions to produce per result
	 * @return the number of suggestions that were created
	 */
	int addSuggestions(Model results, int maxCount, Function<RDFNode,String> labelFunction);

	
	/**
	 * Adds dash:suggestion triples for a given result resource.
	 * @param result  the sh:ValidationResult to add the suggestions to
	 * @param maxCount  the maximum number of suggestions to produce
	 * @return the number of suggestions that were created
	 */
	int addSuggestions(Resource result, int maxCount, Function<RDFNode,String> labelFunction);
	
	
	/**
	 * Checks if this is (in principle) capable of adding suggestions for a given result.
	 * @param result  the validation result
	 * @return true if this can
	 */
	boolean canHandle(Resource result);
}
