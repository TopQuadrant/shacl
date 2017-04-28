package org.topbraid.shacl.validation.js;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHParameterizableTarget;
import org.topbraid.shacl.validation.TargetPlugin;
import org.topbraid.shacl.vocabulary.SH;

public class JSTargetPlugin implements TargetPlugin {

	@Override
	public boolean canExecuteTarget(Resource target) {
		return target.hasProperty(SH.jsFunctionName);
	}

	
	@Override
	public Iterable<RDFNode> executeTarget(Dataset dataset, Resource target,
			SHParameterizableTarget parameterizableTarget) {
		// TODO: Implement
		throw new UnsupportedOperationException("JavaScript-based targets implemented yet");
	}


	@Override
	public boolean isNodeInTarget(RDFNode focusNode, Dataset dataset, Resource executable, SHParameterizableTarget parameterizableTarget) {
		// TODO: Implement
		throw new UnsupportedOperationException("JavaScript-based targets implemented yet");
	}
}
