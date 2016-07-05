package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHResource;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.impl.ResourceImpl;

public class SHResourceImpl extends ResourceImpl implements SHResource {
	
	public SHResourceImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}