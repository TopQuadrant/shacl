package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.topbraid.shacl.model.SHParameter;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class SHParameterImpl extends SHPropertyShapeImpl implements SHParameter {
	
	public SHParameterImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	
	
	@Override
	public boolean isOptional() {
		return JenaUtil.getBooleanProperty(this, SH.optional);
	}


	@Override
    public String toString() {
		return "Parameter " + getVarName();
	}
}