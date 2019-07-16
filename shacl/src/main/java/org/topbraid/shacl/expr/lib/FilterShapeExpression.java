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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.topbraid.shacl.expr.AbstractInputExpression;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionVisitor;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.validation.ValidationEngineFactory;
import org.topbraid.shacl.vocabulary.SH;

public class FilterShapeExpression extends AbstractInputExpression {
	
	private Resource filterShape;
	
	
	public FilterShapeExpression(RDFNode expr, NodeExpression nodes, Resource filterShape) {
		super(expr, nodes == null ? new FocusNodeExpression(SH.this_.inModel(expr.getModel())) : nodes);
		this.filterShape = filterShape;
	}


	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		return getInput().eval(focusNode, context).filterKeep(node -> conforms(node, context));
	}
	
	
	public Resource getFilterShape() {
		return filterShape;
	}


	@Override
	public List<String> getFunctionalSyntaxArguments() {
		List<String> results = new ArrayList<>(2);
		results.add(getInput().getFunctionalSyntax());
		results.add(filterShape.toString()); // TODO!
		return results;
	}
	
	
	@Override
	public List<NodeExpression> getInputExpressions() {
		List<NodeExpression> is = super.getInputExpressions();
		if(is.size() == 1 && is.get(0) instanceof FocusNodeExpression) {
			return Collections.emptyList();
		}
		else {
			return is;
		}
	}


	@Override
	public Resource getOutputShape(Resource contextShape) {
		return getInput().getOutputShape(contextShape);
	}

	
	@Override
	public String getTypeId() {
		return "filterShape";
	}


	private boolean conforms(RDFNode node, NodeExpressionContext context) {
		ValidationEngine engine = ValidationEngineFactory.get().create(context.getDataset(), context.getShapesGraphURI(), context.getShapesGraph(), null);
		return engine.nodesConformToShape(Collections.singletonList(node), filterShape.asNode());
	}
	
	
	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
