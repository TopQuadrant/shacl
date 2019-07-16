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

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterExtendByVar;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.topbraid.jenax.util.DatasetWithDifferentDefaultModel;
import org.topbraid.shacl.util.SHACLUtil;

/**
 * The property function tosh:targetContains.
 * Binds the variable on the right hand side with all focus nodes produced by the
 * SHACL target on the left hand side.
 * 
 * 		(?myTarget ?shapesGraph) tosh:targetContains ?focusNode .
 * 
 * @author Holger Knublauch
 */
public class TargetContainsPFunction extends PropertyFunctionBase {
	
	@Override
	public QueryIterator exec(Binding binding, PropFuncArg argSubject,
			Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {

		argSubject = Substitute.substitute(argSubject, binding);
		argObject = Substitute.substitute(argObject, binding);
		
		if(!argObject.getArg().isVariable()) {
			throw new ExprEvalException("Right hand side of tosh:targetContains must be a variable");
		}
		
		Node targetNode = argSubject.getArgList().get(0);
		Node shapesGraphNode = argSubject.getArgList().get(1);
		
		Model currentModel = ModelFactory.createModelForGraph(execCxt.getActiveGraph());
		Dataset dataset = new DatasetWithDifferentDefaultModel(currentModel, DatasetImpl.wrap(execCxt.getDataset()));

		Model model = dataset.getNamedModel(shapesGraphNode.getURI());
		Resource target = (Resource) model.asRDFNode(targetNode);

		Set<Node> focusNodes = new HashSet<Node>();
		SHACLUtil.addNodesInTarget(target, dataset, focusNodes);
		return new QueryIterExtendByVar(binding, (Var) argObject.getArg(), focusNodes.iterator(), execCxt);
	}
}
