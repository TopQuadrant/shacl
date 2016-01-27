package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLShape;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

public class SHACLShapeImpl extends SHACLResourceImpl implements SHACLShape {

	public SHACLShapeImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}
