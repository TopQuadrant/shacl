package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLClass;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaDatatypes;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;

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