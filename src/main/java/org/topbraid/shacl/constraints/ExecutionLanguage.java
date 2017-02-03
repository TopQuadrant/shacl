package org.topbraid.shacl.constraints;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHConstraint;
import org.topbraid.shacl.model.SHParameterizableTarget;

/**
 * A processor that can execute constraint and target executables.
 * The default implementation uses SPARQL, based on sh:sparql.
 * 
 * @author Holger Knublauch
 */
public interface ExecutionLanguage {
	
	// TODO: This is currently very messy and requires substantial cleaning up to
	//       the various changes of the metamodel over time.
	
	SHConstraint asConstraint(Resource c);
	
	boolean canExecuteConstraint(ConstraintExecutable executable);
	
	boolean canExecuteTarget(Resource executable);
	
	// Returns false if no violations/failures were found
	boolean executeConstraint(Dataset dataset, Resource shape, URI shapesGraphURI, ConstraintExecutable executable, RDFNode focusNode, Resource report, Function<RDFNode,String> labelFunction, List<Resource> resultsList);

	Iterable<RDFNode> executeTarget(Dataset dataset, Resource executable, SHParameterizableTarget parameterizableTarget);
	
	Resource getConstraintComponent();
	
	Resource getExecutableType();
	
	Property getParameter();
	
	boolean isNodeInTarget(RDFNode focusNode, Dataset dataset, Resource executable, SHParameterizableTarget parameterizableTarget);
}
