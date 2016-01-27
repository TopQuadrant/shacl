package org.topbraid.spin.arq.functions;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.topbraid.spin.model.TemplateCall;

import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.pfunction.PropFuncArg;

public class SPINFunctionUtil {

	static void addBindingsFromTemplateCall(QuerySolutionMap initialBinding, TemplateCall templateCall) {
		Map<String,RDFNode> tbs = templateCall.getArgumentsMapByVarNames();
		for(String varName : tbs.keySet()) {
			if(!initialBinding.contains(varName)) {
				initialBinding.add(varName, tbs.get(varName));
			}
		}
	}

	
	static QuerySolutionMap getInitialBinding(Node[] nodes, Model model) {
		QuerySolutionMap map = new QuerySolutionMap();
		for(int i = 1; i < nodes.length - 1; i++) {
			Node varNameNode = nodes[i++];
			Node valueNode = nodes[i];
			if(valueNode.isConcrete()) {
				String varName = varNameNode.getLiteralLexicalForm();
				map.add(varName, model.asRDFNode(valueNode));
			}
		}
		return map;
	}

	
	public static List<Node> getNodes(PropFuncArg arg) {
		if(arg.isNode()) {
			return Collections.singletonList(arg.getArg());
		}
		else {
			return arg.getArgList();
		}
	}

	
	static Resource getQueryOrTemplateCall(PropFuncArg subject, Model model) {
		Node node;
		if(subject.isNode()) {
			node = subject.getArg();
		}
		else {
			node = subject.getArg(0);
		}
		if(node == null || node.isLiteral() || node.isVariable()) {
			throw new ExprEvalException("First argument must be a sp:Select");
		}
		return model.asRDFNode(node).asResource();
	}
}
