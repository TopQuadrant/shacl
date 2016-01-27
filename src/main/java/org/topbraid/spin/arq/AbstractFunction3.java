/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.arq;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;


/**
 * An abstract superclass for Functions with 3 arguments.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractFunction3 extends AbstractFunction {

	@Override
	protected NodeValue exec(Node[] nodes, FunctionEnv env) {
		Node arg1 = nodes.length > 0 ? nodes[0] : null;
		Node arg2 = nodes.length > 1 ? nodes[1] : null;
		Node arg3 = nodes.length > 2 ? nodes[2] : null;
		return exec(arg1, arg2, arg3, env);
	}
	
	
	protected abstract NodeValue exec(Node arg1, Node arg2, Node arg3, FunctionEnv env);
}
