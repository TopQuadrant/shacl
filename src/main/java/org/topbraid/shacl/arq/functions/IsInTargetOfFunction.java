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
package org.topbraid.shacl.arq.functions;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.topbraid.jenax.functions.AbstractFunction2;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHShape;

/**
 * The function tosh:isInTargetOf.
 * 
 * @author Holger Knublauch
 */
public class IsInTargetOfFunction extends AbstractFunction2 {

	@Override
	protected NodeValue exec(Node nodeNode, Node shapeNode, FunctionEnv env) {
		Model model = ModelFactory.createModelForGraph(env.getActiveGraph());
		SHShape shape = SHFactory.asShape(model.asRDFNode(shapeNode));
		return NodeValue.makeBoolean(shape.hasTargetNode(model.asRDFNode(nodeNode)));
	}
}
