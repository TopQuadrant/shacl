package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLShape;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;

public class SHACLShapeImpl extends SHACLResourceImpl implements SHACLShape {

	public SHACLShapeImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}
