package org.topbraid.shacl.constraints;

import java.net.URI;
import java.util.function.Function;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHParameterizableScope;

/**
 * A processor that can execute constraint and scope executables.
 * The default implementation uses SPARQL, based on sh:sparql.
 * 
 * @author Holger Knublauch
 */
public interface ExecutionLanguage {
	
	boolean canExecuteConstraint(ConstraintExecutable executable);
	
	boolean canExecuteScope(Resource executable);
	
	void executeConstraint(Dataset dataset, Resource shape, URI shapesGraphURI, ConstraintExecutable executable, RDFNode focusNode, Model results, Function<RDFNode,String> labelFunction);

	Iterable<RDFNode> executeScope(Dataset dataset, Resource executable, SHParameterizableScope parameterizableScope);
	
	boolean isNodeInScope(RDFNode focusNode, Dataset dataset, Resource executable, SHParameterizableScope parameterizableScope);
}
