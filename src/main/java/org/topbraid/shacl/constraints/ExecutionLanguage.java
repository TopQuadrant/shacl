package org.topbraid.shacl.constraints;

import java.net.URI;

import org.topbraid.shacl.model.SHACLConstraint;
import org.topbraid.shacl.model.SHACLTemplateCall;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * A processor that can execute constraint and scope executables.
 * The default implementation uses SPARQL, based on sh:sparql.
 * 
 * @author Holger Knublauch
 */
public interface ExecutionLanguage {
	
	boolean canExecuteConstraint(ConstraintExecutable executable);
	
	boolean canExecuteScope(Resource executable);
	
	void executeConstraint(Dataset dataset, Resource shape, URI shapesGraphURI, SHACLConstraint constraint, ConstraintExecutable executable, RDFNode focusNode, Model results);

	Iterable<RDFNode> executeScope(Dataset dataset, Resource executable, SHACLTemplateCall templateCall);
	
	boolean isNodeInScope(RDFNode focusNode, Dataset dataset, Resource executable, SHACLTemplateCall templateCall);
}
