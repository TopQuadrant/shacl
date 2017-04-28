package org.topbraid.shacl.expr;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.arq.SHACLPaths;

public class PathExpression implements NodeExpression {
	
	private Resource path;
	
	
	public PathExpression(Resource path) {
		this.path = path;
	}

	
	@Override
	public List<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		List<RDFNode> results = new LinkedList<>();
		SHACLPaths.addValueNodes(focusNode.inModel(context.getDataset().getDefaultModel()), path, results);
		return results;
	}

	
	@Override
	public String toString() {
		return "Values of " + SHACLPaths.getPathString(path);
	}
}
