package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLResource;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

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