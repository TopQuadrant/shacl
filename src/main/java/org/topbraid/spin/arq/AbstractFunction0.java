/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.arq;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;


/**
 * An abstract superclass for functions with 0 arguments.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractFunction0 extends AbstractFunction {

	@Override
	protected NodeValue exec(Node[] nodes, FunctionEnv env) {
		return exec(env);
	}
	
	
	protected abstract NodeValue exec(FunctionEnv env);
}
