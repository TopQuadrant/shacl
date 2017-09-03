package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHNodeShape;
import org.topbraid.shacl.vocabulary.SH;

public class SHNodeShapeImpl extends SHShapeImpl implements SHNodeShape {

	public SHNodeShapeImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public Resource getContext() {
		return SH.NodeShape.inModel(getModel());
	}
}
