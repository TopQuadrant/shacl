package org.topbraid.shacl.model;

import java.util.Map;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;

public interface SHParameterizableInstance extends SHResource {
	
	
	void addBindings(QuerySolutionMap bindings);

	
	SHParameterizable getParameterizable();

	
	/**
	 * Gets a Map from variable names to RDFNodes derived from the Parameters.
	 * @return a Map from variable names to RDFNodes
	 */
	Map<String,RDFNode> getParameterMapByVarNames();
}
