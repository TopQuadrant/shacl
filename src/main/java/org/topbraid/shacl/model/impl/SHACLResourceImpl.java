package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLResource;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.impl.ResourceImpl;

/**
 * Default implementation of SHACLResource.
 * 
 * @author Holger Knublauch
 */
public class SHACLResourceImpl extends ResourceImpl implements SHACLResource {
	
	public SHACLResourceImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}