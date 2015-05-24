package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLFunction;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;

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