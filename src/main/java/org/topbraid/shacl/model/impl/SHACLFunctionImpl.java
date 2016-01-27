package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLFunction;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;

/**
 * Default implementation of SHACLFunction.
 * 
 * @author Holger Knublauch
 */
public class SHACLFunctionImpl extends SHACLMacroImpl implements SHACLFunction {
	
	public SHACLFunctionImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
}