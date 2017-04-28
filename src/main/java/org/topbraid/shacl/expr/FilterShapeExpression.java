package org.topbraid.shacl.expr;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.validation.ValidationEngineFactory;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.system.SPINLabels;

public class FilterShapeExpression implements NodeExpression {
	
	private Resource filterShape;
	
	private NodeExpression nodes;
	
	private ShapesGraph shapesGraph;
	
	
	public FilterShapeExpression(NodeExpression nodes, Resource filterShape, ShapesGraph shapesGraph) {
		this.nodes = nodes;
		this.filterShape = filterShape;
		this.shapesGraph = shapesGraph;
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
		ValidationEngine engine = ValidationEngineFactory.get().create(context.getDataset(), context.getShapesGraphURI(), shapesGraph, null);
		Resource report = engine.validateNodesAgainstShape(Collections.singletonList(node), filterShape.asNode());
		return !report.hasProperty(SH.result);
	}

	
	@Override
	public String toString() {
		return "hasShape(" + SPINLabels.get().getNodeLabel(filterShape) + ", " + nodes + ")";
	}
}
