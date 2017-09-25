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

package org.topbraid.spin.arq.functions;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;

/**
 * The SPARQL function spin:eval.
 * 
 * The first argument is a SPIN expression, e.g. a function call or variable.
 * All other arguments must come in pairs, alternating between an argument property
 * and its value, e.g.
 * 
 *  	spin:eval(ex:myInstance, sp:arg3, "value")
 *  
 * The expression will be evaluated with all bindings from the property-value pairs above.
 * 
 * @author Holger Knublauch
 */
public class EvalFunction extends AbstractEvalFunction {
	
	
	public EvalFunction() {
		super(1);
	}

	
	@Override
	public NodeValue exec(Node[] nodes, FunctionEnv env) {

		Model baseModel = ModelFactory.createModelForGraph(env.getActiveGraph());
		return exec(baseModel, nodes, env);
	}
}
