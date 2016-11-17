package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHResult;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class SHResultImpl extends SHResourceImpl implements SHResult {
	
	public SHResultImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public RDFNode getFocusNode() {
		return JenaUtil.getProperty(this, SH.focusNode);
	}

	
	@Override
	public String getMessage() {
		return JenaUtil.getStringProperty(this, SH.resultMessage);
	}

	
	@Override
	public Resource getPath() {
		return JenaUtil.getPropertyResourceValue(this, SH.resultPath);
	}

	
	@Override
	public RDFNode getValue() {
		return JenaUtil.getProperty(this, SH.value);
	}

	
	@Override
	public Resource getSourceConstraint() {
		return JenaUtil.getResourceProperty(this, SH.sourceConstraint);
	}

	
	@Override
	public Resource getSourceConstraintComponent() {
		return JenaUtil.getResourceProperty(this, SH.sourceConstraintComponent);
	}

	
	@Override
	public Resource getSourceShape() {
		return JenaUtil.getResourceProperty(this, SH.sourceShape);
	}
}