package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLClass;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaDatatypes;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

/**
 * Default implementation of SHACLClass.
 * 
 * @author Holger Knublauch
 */
public class SHACLClassImpl extends SHACLResourceImpl implements SHACLClass {
	
	public SHACLClassImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public boolean isAbstract() {
		return hasProperty(SH.abstract_, JenaDatatypes.TRUE);
	}
}