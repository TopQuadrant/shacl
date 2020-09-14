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

import java.net.URI;
import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterExtendByVar;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.engine.ShapesGraphFactory;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionFactory;

/**
 * The property function tosh:evalExpr.
 * Binds the variable on the right hand side with all nodes produced by evaluating
 * the given node expression against the given focus node
 * 
 * 		(?expr ?focusNode) tosh:evalExpr ?result .
 * 
 * @author Holger Knublauch
 */
public class EvalExprPFunction extends PropertyFunctionBase {
	
	@Override
	public QueryIterator exec(Binding binding, PropFuncArg argSubject,
			Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {

		argSubject = Substitute.substitute(argSubject, binding);
		argObject = Substitute.substitute(argObject, binding);
		
		if(!argObject.getArg().isVariable()) {
			throw new ExprEvalException("Right hand side of tosh:exprEval must be a variable");
		}
		
		Node exprNode = argSubject.getArgList().get(0);
		Node focusNode = argSubject.getArgList().get(1);
		
		Model model = ModelFactory.createModelForGraph(execCxt.getActiveGraph());
		Dataset dataset = ARQFactory.get().getDataset(model);
		URI shapesGraphURI = URI.create("urn:x-topbraid:dummyShapesGraph");
		dataset.addNamedModel(shapesGraphURI.toString(), model);
		
		ShapesGraph[] shapesGraph = new ShapesGraph[1];
		
		NodeExpression n = NodeExpressionFactory.get().create(model.asRDFNode(exprNode));
		ExtendedIterator<RDFNode> it = n.eval(model.asRDFNode(focusNode), new NodeExpressionContext() {
			
			@Override
			public URI getShapesGraphURI() {
				return shapesGraphURI;
			}
			
			@Override
			public ShapesGraph getShapesGraph() {
				if(shapesGraph[0] == null) {
					shapesGraph[0] = ShapesGraphFactory.get().createShapesGraph(model);
				}
				return shapesGraph[0];
			}
			
			@Override
			public Dataset getDataset() {
				return dataset;
			}
		});
		
		Iterator<Node> nit = it.mapWith(rdfNode -> rdfNode.asNode());

		return new QueryIterExtendByVar(binding, (Var) argObject.getArg(), nit, execCxt);
	}
}
