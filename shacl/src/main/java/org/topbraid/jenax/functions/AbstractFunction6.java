/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.topbraid.jenax.functions;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;


/**
 * An abstract superclass for Functions with 6 arguments.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractFunction6 extends AbstractFunction {

	@Override
	protected NodeValue exec(Node[] nodes, FunctionEnv env) {
		Node arg1 = nodes.length > 0 ? nodes[0] : null;
		Node arg2 = nodes.length > 1 ? nodes[1] : null;
		Node arg3 = nodes.length > 2 ? nodes[2] : null;
		Node arg4 = nodes.length > 3 ? nodes[3] : null;
		Node arg5 = nodes.length > 4 ? nodes[4] : null;
		Node arg6 = nodes.length > 5 ? nodes[5] : null;
		return exec(arg1, arg2, arg3, arg4, arg5, arg6, env);
	}
	
	
	protected abstract NodeValue exec(Node arg1, Node arg2, Node arg3, Node arg4, Node arg5, Node arg6, FunctionEnv env);
}
