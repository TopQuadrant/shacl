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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.topbraid.shacl.expr.AppendContext;
import org.topbraid.shacl.expr.ComplexNodeExpression;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionVisitor;
import org.topbraid.shacl.expr.SNEL;

public class IntersectionExpression extends ComplexNodeExpression {
	
	private List<NodeExpression> inputs;
	
	
	public IntersectionExpression(RDFNode expr, List<NodeExpression> inputs) {
		super(expr);
		this.inputs = inputs;
	}

	
	@Override
	public void appendSPARQL(AppendContext context, String targetVarName) {
		String varName = context.getNextVarName();
		for(int i = 0; i < inputs.size(); i++) {
			NodeExpression input = inputs.get(i);
			if(input instanceof ComplexNodeExpression) {
				((ComplexNodeExpression)input).appendSPARQL(context, varName + (i + 1));
			}
			else {
				context.indent();
				context.append("BIND (");
				context.append(input.toString());
				context.append(" AS ?");
				context.append(varName + (i + 1));
				context.append(") .\n");
			}
		}
		context.indent();
		context.append("FILTER (bound(?");
		context.append(varName);
		context.append("1) ");
		for(int i = 1; i < inputs.size(); i++) {
			context.append(" && ?");
			context.append(varName + (i + 1));
			context.append("=?");
			context.append(varName + "1");
		}
		context.append(") .\n");
		context.indent();
		context.append("BIND (?");
		context.append(varName);
		context.append("1 AS ?");
		context.append(targetVarName);
		context.append(") .\n");
	}


	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		Iterator<NodeExpression> it = inputs.iterator();
		if(it.hasNext()) {
			// TODO: maintain order
			NodeExpression first = it.next();
			Set<RDFNode> results = new HashSet<>(first.eval(focusNode, context).toList());
			while(it.hasNext()) {
				NodeExpression next = it.next();
				results.retainAll(next.eval(focusNode, context).toList());
			}
			return WrappedIterator.create(results.iterator());
		}
		else {
			return WrappedIterator.emptyIterator();
		}
	}


	@Override
	public List<String> getFunctionalSyntaxArguments() {
		List<String> results = new LinkedList<>();
		for(NodeExpression expr : inputs) {
			results.add(expr.getFunctionalSyntax());
		}
		return results;
	}
	
	
	@Override
	public List<NodeExpression> getInputExpressions() {
		return inputs;
	}

	
	@Override
	public Resource getOutputShape(Resource contextShape) {
		if(inputs.size() == 0) {
			return null;
		}
		Resource s = inputs.get(0).getOutputShape(contextShape);
		if(s == null) {
			return null;
		}
		for(int i = 1; i < inputs.size(); i++) {
			Resource o = inputs.get(i).getOutputShape(contextShape);
			if(!s.equals(o)) {
				return null;
			}
		}
		return s;
	}


	@Override
	public SNEL getTypeId() {
		return SNEL.intersection;
	}
	
	
	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
