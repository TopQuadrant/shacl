package org.topbraid.shacl.validation;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHParameterizableTarget;

public interface TargetPlugin {
	
	boolean canExecuteTarget(Resource target);

	Iterable<RDFNode> executeTarget(Dataset dataset, Resource executable, SHParameterizableTarget parameterizableTarget);
	
	boolean isNodeInTarget(RDFNode focusNode, Dataset dataset, Resource executable, SHParameterizableTarget parameterizableTarget);
}
