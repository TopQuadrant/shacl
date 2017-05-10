package org.topbraid.shacl.model;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public interface SHRule extends SHResource {
	
	/**
	 * Gets the sh:subject (assuming this is a triple rule)
	 * @return the subject of the triple rule
	 */
	RDFNode getSubject();
	
	/**
	 * Gets the sh:subject (assuming this is a triple rule)
	 * @return the subject of the triple rule
	 */
	Resource getPredicate();
	
	/**
	 * Gets the sh:subject (assuming this is a triple rule)
	 * @return the subject of the triple rule
	 */
	RDFNode getObject();
	
	
	/**
	 * Checks if this rule is an instance of sh:JSRule
	 * @return true if this is a sh:JSRule
	 */
	boolean isJSRule();

	
	/**
	 * Checks if this rule is an instance of sh:SPARQLRule
	 * @return true if this is a sh:SPARQLRule
	 */
	boolean isSPARQLRule();

	
	/**
	 * Checks if this rule is an instance of sh:TripleRule
	 * @return true if this is a sh:TripleRule
	 */
	boolean isTripleRule();
}
