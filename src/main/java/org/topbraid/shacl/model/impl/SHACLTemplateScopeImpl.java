package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLTemplateScope;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

/**
 * Default implementation of SHACLTemplateScope.
 * 
 * @author Holger Knublauch
 */
public class SHACLTemplateScopeImpl extends SHACLTemplateCallImpl implements SHACLTemplateScope {
	
	public SHACLTemplateScopeImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}