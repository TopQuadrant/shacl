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
package org.topbraid.shacl.expr;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.validation.ValidationEngineFactory;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.jenax.util.RDFLabels;

public class FilterShapeExpression extends ComplexNodeExpression {
	
	private Resource filterShape;
	
	private NodeExpression nodes;
	
	
	public FilterShapeExpression(NodeExpression nodes, Resource filterShape) {
		this.nodes = nodes;
		this.filterShape = filterShape;
	}
	

	@Override
	public void appendLabel(AppendContext context, String targetVarName) {
		if(nodes instanceof ComplexNodeExpression) {
			((ComplexNodeExpression)nodes).appendLabel(context, targetVarName);
		}
		context.indent();
		context.append("FILTER tosh:hasShape(");
		if(nodes instanceof AtomicNodeExpression) {
			context.append(nodes.toString());
		}
		else {
			context.append("?" + targetVarName);
		}
		context.append(", ");
		if(filterShape.isURIResource()) {
			context.append(RDFLabels.get().getLabel(filterShape));
		}
		else {
			context.append("_:" + filterShape.asNode().getBlankNodeLabel());
		}
		context.append(") .\n");
	}


	@Override
	public List<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		List<RDFNode> results = new LinkedList<>();
		for(RDFNode node : nodes.eval(focusNode, context)) {
			if(conforms(node, context)) {
				results.add(node);
			}
		}
		return results;
	}
	
	
	private boolean conforms(RDFNode node, NodeExpressionContext context) {
		ValidationEngine engine = ValidationEngineFactory.get().create(context.getDataset(), context.getShapesGraphURI(), context.getShapesGraph(), null);
		Resource report = engine.validateNodesAgainstShape(Collections.singletonList(node), filterShape.asNode());
		return !report.hasProperty(SH.result);
	}
}
