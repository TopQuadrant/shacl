/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.arq;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;


/**
 * An abstract superclass for Functions with 1 argument.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractFunction1 extends AbstractFunction {

	@Override
	protected NodeValue exec(Node[] nodes, FunctionEnv env) {
		Node arg1 = nodes.length > 0 ? nodes[0] : null;
		return exec(arg1, env);
	}
	
	
	protected abstract NodeValue exec(Node arg1, FunctionEnv env);
}
