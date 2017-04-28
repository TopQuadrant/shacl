package org.topbraid.shacl.expr;

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
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.topbraid.spin.system.SPINLabels;

public class FunctionExpression implements NodeExpression {
	
	private List<NodeExpression> args;
	
	private Expr expr;
	
	private Resource function;
	
	
	public FunctionExpression(Resource function, List<NodeExpression> args) {
		this.args = args;
		this.function = function;
		
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

	
	@Override
	public List<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		List<RDFNode> results = new LinkedList<>();
		
		Context cxt = ARQ.getContext().copy();
		cxt.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime());

		int total = 1;
		List<List<RDFNode>> as = new LinkedList<>();
		for(NodeExpression expr : args) {
			List<RDFNode> a = expr.eval(focusNode, context);
			if(a.isEmpty()) {
				// TODO: check optional values
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
				int m = y % a.size();
				if(!a.isEmpty()) {
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
		return results;
	}

	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(SPINLabels.get().getLabel(function));
		sb.append("(");
		for(int i = 0; i < args.size(); i++) {
			if(i > 0) {
				sb.append(", ");
			}
			sb.append(args.get(i).toString());
		}
		sb.append(")");
		return sb.toString();
	}
}
