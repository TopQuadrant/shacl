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
package org.topbraid.shacl.expr.lib;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.FunctionFactory;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.sse.ItemList;
import org.apache.jena.sparql.sse.builders.BuilderExpr;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.topbraid.jenax.functions.OptionalArgsFunction;
import org.topbraid.shacl.expr.ComplexNodeExpression;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionVisitor;
import org.topbraid.shacl.vocabulary.SPARQL;

public class FunctionExpression extends ComplexNodeExpression {
	
	private List<NodeExpression> args;
	
	private static MyBuilderExpr bob = new MyBuilderExpr() ;
	
	private Expr expr;
	
	private Resource function;
	
	
	public FunctionExpression(RDFNode expr, Resource function, List<NodeExpression> args) {
		
		super(expr);
		
		this.args = args;
		this.function = function;
		
		if(function.getNameSpace().equals(SPARQL.NS)) {
			ItemList il = new ItemList();
			il.add(function.asNode());
			for(int i = 0; i < args.size(); i++) {
				il.add(Var.alloc("a" + i));
			}
			BuilderExpr.Build build = bob.getBuild(function.getLocalName());
			if(build == null) {
				throw new IllegalArgumentException("Unknown SPARQL built-in " + function.getLocalName());
			}
			this.expr = build.make(il);
		}
		else {
			StringBuffer sb = new StringBuffer();
			sb.append("<");
			sb.append(function);
			sb.append(">(");
			for(int i = 0; i < args.size(); i++) {
				if(i > 0) {
					sb.append(",");
				}
				sb.append("?a" + i);
			}
			sb.append(")");
			this.expr = ExprUtils.parse(sb.toString());
		}
	}


	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		List<RDFNode> results = new LinkedList<>();
		
		Context cxt = ARQ.getContext().copy();
		cxt.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime());

		OptionalArgsFunction opt = null;
		FunctionFactory ff = FunctionRegistry.get().get(function.getURI());
		if(ff != null) {
			Function arq = ff.create(function.getURI());
			if(arq instanceof OptionalArgsFunction) {
				opt = (OptionalArgsFunction) arq;
			}
		}
		int total = 1;
		List<List<RDFNode>> as = new LinkedList<>();
		for(int i = 0; i < args.size(); i++) {
			NodeExpression expr = args.get(i);
			List<RDFNode> a = expr.eval(focusNode, context).toList();
			if(a.isEmpty()) {
				if(opt == null || !opt.isOptionalArg(i)) {
					return WrappedIterator.create(results.iterator());
				}
			}
			else {
				total *= a.size();
			}
			as.add(a);
		}
		
		for(int x = 0; x < total; x++) {
			
			int y = x;
			BindingHashMap binding = new BindingHashMap();
			for(int i = 0; i < args.size(); i++) {
				List<RDFNode> a = as.get(i);
				if(!a.isEmpty()) {
					int m = y % a.size();
					binding.add(Var.alloc("a" + i), a.get(m).asNode());
					y /= a.size();
				}
			}
			
			Dataset dataset = context.getDataset();
			DatasetGraph dsg = dataset.asDatasetGraph();
			FunctionEnv env = new ExecutionContext(cxt, dsg.getDefaultGraph(), dsg, null);
			try {
				NodeValue r = expr.eval(binding, env);
				if(r != null) {
					Model defaultModel = dataset.getDefaultModel();
					RDFNode rdfNode = defaultModel.asRDFNode(r.asNode());
					if(!results.contains(rdfNode)) {
						results.add(rdfNode);
					}
				}
			}
			catch(ExprEvalException ex) {
			}
		}
		return WrappedIterator.create(results.iterator());
	}
	
	
	public Resource getFunction() {
		return function;
	}


	@Override
	public List<String> getFunctionalSyntaxArguments() {
		List<String> results = new LinkedList<>();
		results.add(FmtUtils.stringForRDFNode(function));
		for(NodeExpression arg : args) {
			results.add(arg.getFunctionalSyntax());
		}
		return results;
	}
	
	
	@Override
	public List<NodeExpression> getInputExpressions() {
		return args;
	}


	@Override
	public String getTypeId() {
		return "function";
	}


	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
	
	
	private static class MyBuilderExpr extends BuilderExpr {
		
		BuilderExpr.Build getBuild(String name) {
			return dispatch.get(name);
		}
	}
}
