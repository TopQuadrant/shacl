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
import org.apache.jena.sparql.util.IterLib;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.engine.ShapesGraphFactory;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.PathEvaluator;

/**
 * The property function tosh:values.
 * Binds the variable on the right hand side with all values of a given predicate at a given focus node,
 * including any values inferred by sh:values statements.
 * 
 * 		(?focusNode ?predicate) tosh:values ?result .
 * 
 * Also works for some cases if the right hand side is concrete.
 * 
 * @author Holger Knublauch
 */
public class ValuesPFunction extends PropertyFunctionBase {
	
	@Override
	public QueryIterator exec(Binding binding, PropFuncArg argSubject,
			Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {

		argSubject = Substitute.substitute(argSubject, binding);
		argObject = Substitute.substitute(argObject, binding);
		
		if(!argSubject.isList() || argSubject.getArgListSize() != 2) {
			throw new ExprEvalException("Left hand side of tosh:values must be a list with two members");
		}
		
		Node focusNode = argSubject.getArgList().get(0);
		Node predicateNode = argSubject.getArgList().get(1);
		
		if(predicateNode.isVariable()) {
			return IterLib.noResults(execCxt);
		}
		
		Model model = ModelFactory.createModelForGraph(execCxt.getActiveGraph());
		Dataset dataset = ARQFactory.get().getDataset(model);
		URI shapesGraphURI = URI.create("urn:x-topbraid:dummyShapesGraph");
		dataset.addNamedModel(shapesGraphURI.toString(), model);
		ShapesGraph shapesGraph = ShapesGraphFactory.get().createShapesGraph(model);
		
		PathEvaluator eval = new PathEvaluator(model.getProperty(predicateNode.getURI()));
		
		NodeExpressionContext context = new NodeExpressionContext() {
			
			@Override
			public URI getShapesGraphURI() {
				return shapesGraphURI;
			}
			
			@Override
			public ShapesGraph getShapesGraph() {
				return shapesGraph;
			}
			
			@Override
			public Dataset getDataset() {
				return dataset;
			}
		};
		
		if(argObject.getArg().isVariable()) {
			if(focusNode.isVariable()) {
				// Both subject and object are variables -> return nothing
				return IterLib.noResults(execCxt);
			}
			else {
				// subject is concrete, object is variable -> iterate over values with subject as focus node
				ExtendedIterator<RDFNode> it = eval.eval(model.asRDFNode(focusNode), context);
				Iterator<Node> nit = it.mapWith(rdfNode -> rdfNode.asNode());
				return new QueryIterExtendByVar(binding, (Var) argObject.getArg(), nit, execCxt);
			}
		}
		else if(focusNode.isVariable()) {
			// subject is variable, object is concrete
			if(eval.isReversible(shapesGraph)) {
				// If possible, evaluate in the reverse direction to compute subjects from objects
				ExtendedIterator<RDFNode> it = eval.evalReverse(model.asRDFNode(argObject.getArg()), context);
				Iterator<Node> nit = it.mapWith(rdfNode -> rdfNode.asNode());
				return new QueryIterExtendByVar(binding, (Var) focusNode, nit, execCxt);
			}
			else {
				return IterLib.noResults(execCxt);
			}
		}
		else {
			// Both subject and object are concrete -> continue if subject (as focus node) as object as value
			// This could be optimized further into a direct "hasValue" look-up in the future
			ExtendedIterator<RDFNode> it = eval.eval(model.asRDFNode(focusNode), context);
			while(it.hasNext()) {
				RDFNode n = it.next();
				if(n.asNode().equals(argObject.getArg())) {
					it.close();
					return IterLib.result(binding, execCxt);
				}
			}
			return IterLib.noResults(execCxt);
		}
	}
}
