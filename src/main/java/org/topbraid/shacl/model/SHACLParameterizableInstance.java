package org.topbraid.shacl.model;

import java.util.Map;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;

public interface SHACLParameterizableInstance extends SHACLResource {
	
	
	void addBindings(QuerySolutionMap bindings);

	
	SHACLParameterizable getParameterizable();

	
	/**
	 * Gets a Map from variable names to RDFNodes derived from the Parameters.
	 * @return a Map from variable names to RDFNodes
	 */
	Map<String,RDFNode> getParameterMapByVarNames();
}
