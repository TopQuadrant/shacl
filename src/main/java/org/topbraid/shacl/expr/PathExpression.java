package org.topbraid.shacl.expr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.path.Path;
import org.topbraid.shacl.arq.SHACLPaths;

public class PathExpression extends ComplexNodeExpression {
	
	private NodeExpression input;
	
	private Path jenaPath;
	
	private Resource path;
	
	
	public PathExpression(Resource path, NodeExpression input) {
		this.input = input;
		this.path = path;
		if(path.isAnon()) {
			jenaPath = (Path) SHACLPaths.getJenaPath(SHACLPaths.getPathString(path), path.getModel());
		}
	}

	
	@Override
	public void appendLabel(AppendContext context, String targetVarName) {
		if(input instanceof ComplexNodeExpression) {
			String varName = context.getNextVarName();
			((ComplexNodeExpression)input).appendLabel(context, varName);
			context.indent();
			context.append("?" + varName);
			context.append(" ");
			context.append(SHACLPaths.getPathString(path));
			context.append(" ");
			context.append("?" + targetVarName);
		}
		else {
			context.indent();
			if(input instanceof AtomicNodeExpression) {
				context.append(input.toString());
			}
			else {
				context.append("$this");
			}
			context.append(" ");
			context.append(SHACLPaths.getPathString(path));
			context.append(" ");
			context.append("?" + targetVarName);
		}
		context.append(" .\n");
	}


	@Override
	public List<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		if(input != null) {
			Set<RDFNode> results = new HashSet<>();
			if(jenaPath == null) {
				for(RDFNode node : input.eval(focusNode, context)) {
					SHACLPaths.addValueNodes(node.inModel(context.getDataset().getDefaultModel()), path, results);
				}
			}
			else {
				for(RDFNode node : input.eval(focusNode, context)) {
					SHACLPaths.addValueNodes(node.inModel(context.getDataset().getDefaultModel()), jenaPath, results);
				}
			}
			return new ArrayList<RDFNode>(results);
		}
		else {
			List<RDFNode> results = new LinkedList<>();
			if(jenaPath == null) {
				SHACLPaths.addValueNodes(focusNode.inModel(context.getDataset().getDefaultModel()), path, results);
			}
			else {
				SHACLPaths.addValueNodes(focusNode.inModel(context.getDataset().getDefaultModel()), jenaPath, results);
			}
			return results;
		}
	}
}
