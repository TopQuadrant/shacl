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

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;
import org.topbraid.shacl.expr.ComplexNodeExpression;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionVisitor;

public class UnionExpression extends ComplexNodeExpression {
	
	private List<NodeExpression> inputs;
	
	
	public UnionExpression(RDFNode expr, List<NodeExpression> inputs) {
		super(expr);
		this.inputs = inputs;
	}

	
	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		ExtendedIterator<RDFNode> result = NullIterator.instance();
        for(NodeExpression i : inputs) {
        	ExtendedIterator<RDFNode> it = i.eval(focusNode, context);
            result = result.andThen(it);
        }
		return result;
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
	public String getTypeId() {
		return "union";
	}
	
	
	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
