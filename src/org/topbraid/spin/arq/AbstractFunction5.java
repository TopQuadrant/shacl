/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.arq;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;


/**
 * An abstract superclass for Functions with 5 arguments.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractFunction5 extends AbstractFunction {

	@Override
	protected NodeValue exec(Node[] nodes, FunctionEnv env) {
		Node arg1 = nodes.length > 0 ? nodes[0] : null;
		Node arg2 = nodes.length > 1 ? nodes[1] : null;
		Node arg3 = nodes.length > 2 ? nodes[2] : null;
		Node arg4 = nodes.length > 3 ? nodes[3] : null;
		Node arg5 = nodes.length > 4 ? nodes[4] : null;
		return exec(arg1, arg2, arg3, arg4, arg5, env);
	}
	
	
	protected abstract NodeValue exec(Node arg1, Node arg2, Node arg3, Node arg4, Node arg5, FunctionEnv env);
}
