package org.topbraid.shacl.model;

import java.util.Map;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;

public interface SHACLTemplateCall extends SHACLResource {
	
	
	void addBindings(QuerySolutionMap bindings);

	
	SHACLTemplate getTemplate();

	
	/**
	 * Gets a Map from variable names to RDFNodes derived from the Arguments.
	 * @return a Map from variable names to RDFNodes
	 */
	Map<String,RDFNode> getArgumentsMapByVarNames();
}
