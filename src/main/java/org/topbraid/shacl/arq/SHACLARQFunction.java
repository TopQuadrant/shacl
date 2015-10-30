/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.shacl.arq;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;
import org.topbraid.shacl.model.SHACLArgument;
import org.topbraid.shacl.model.SHACLFunction;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.arq.DatasetWithDifferentDefaultModel;
import org.topbraid.spin.arq.SPINFunctionFactory;
import org.topbraid.spin.statistics.SPINStatistics;
import org.topbraid.spin.statistics.SPINStatisticsManager;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.sparql.util.ExprUtils;
import com.hp.hpl.jena.sparql.util.FmtUtils;


/**
 * An ARQ function that delegates its functionality into a user-defined SHACL function.
 * 
 * Use the SHACLFunctions class to install them from their definitions in a Model.
 * 
 * @author Holger Knublauch
 */
public class SHACLARQFunction implements com.hp.hpl.jena.sparql.function.Function, SPINFunctionFactory {
	
	private com.hp.hpl.jena.query.Query arqQuery;
	
	private List<String> argNames = new ArrayList<String>();
	
	private List<Node> argNodes = new ArrayList<Node>();
	
	private boolean cachable;
	
	private String queryString;
	
	private SHACLFunction shaclFunction;
	

	/**
	 * Constructs a new SHACLARQFunction based on a given SHACL Function.
	 * The shaclFunction must be associated with the Model containing
	 * the triples of its definition.
	 * @param shaclFunction  the SHACL function
	 */
	public SHACLARQFunction(SHACLFunction shaclFunction) {
		
		this.shaclFunction = shaclFunction;
		
		this.cachable = shaclFunction.hasProperty(DASH.cachable, JenaDatatypes.TRUE);
		
		try {
			queryString = shaclFunction.getSPARQL();
			arqQuery = ARQFactory.get().createQuery(shaclFunction.getModel(), queryString);
			
			JenaUtil.setGraphReadOptimization(true);
			try {
				for(SHACLArgument arg : shaclFunction.getOrderedArguments()) {
					String varName = arg.getVarName();
					if(varName == null) {
						throw new IllegalStateException("Argument " + arg + " of " + shaclFunction + " does not have a valid predicate");
					}
					argNames.add(varName);
					argNodes.add(arg.getPredicate().asNode());
				}
			}
			finally {
				JenaUtil.setGraphReadOptimization(false);
			}
		}
		catch(Exception ex) {
			throw new IllegalArgumentException("Function " + shaclFunction.getURI() + " does not define a valid body", ex);
		}
	}
	

	public void build(String uri, ExprList args) {
	}

	
	public com.hp.hpl.jena.sparql.function.Function create(String uri) {
		return this;
	}

	
	public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
		
		Graph activeGraph = env.getActiveGraph();
		Model model = activeGraph != null ? 
				ModelFactory.createModelForGraph(activeGraph) :
				ModelFactory.createDefaultModel();
		
		QuerySolutionMap bindings = new QuerySolutionMap();
		
		Node[] argsForCache;
		if(cachable) {
			argsForCache = new Node[args.size()];
		}
		else {
			argsForCache = null;
		}
		for(int i = 0; i < args.size(); i++) {
			Expr expr = args.get(i);
			if(expr != null && (!expr.isVariable() || binding.contains(expr.asVar()))) {
	        	NodeValue x = expr.eval(binding, env);
	        	if(x != null) {
	        		String argName;
	        		if(i < argNames.size()) {
	        			argName = argNames.get(i);
	        		}
	        		else {
	        			argName = "arg" + (i + 1);
	        		}
	        		bindings.add(argName, model.asRDFNode(x.asNode()));
	        		if(cachable) {
	        			argsForCache[i] = x.asNode();
	        		}
	        	}
			}
		}
		
		// TODO: Reactivate
		//if(SPINArgumentChecker.get() != null) {
		//	SPINArgumentChecker.get().check(shaclFunction, bindings);
		//}
		
		Dataset dataset = DatasetImpl.wrap(env.getDataset());
		
		if(SPINStatisticsManager.get().isRecording() && SPINStatisticsManager.get().isRecordingSPINFunctions()) {
			StringBuffer sb = new StringBuffer();
			sb.append("SHACL Function ");
			sb.append(SSE.format(NodeFactory.createURI(uri), model));
			sb.append("(");
			for(int i = 0; i < args.size(); i++) {
				if(i > 0) {
					sb.append(", ");
				}
				Expr expr = args.get(i);
				expr = Substitute.substitute(expr, binding);
				if(expr == null) {
					sb.append("?unbound");
				}
				else {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					IndentedWriter iOut = new IndentedWriter(bos);
					ExprUtils.fmtSPARQL(iOut, expr, new SerializationContext(model));
					iOut.flush();
					sb.append(bos.toString());
				}
			}
			sb.append(")");
			long startTime = System.currentTimeMillis();
			NodeValue result;
			try {
				if(cachable) {
					result = SHACLFunctionsCache.get().execute(this, dataset, model, bindings, argsForCache);
				}
				else {
					result = executeBody(dataset, model, bindings);
				}
				sb.append(" = ");
				sb.append(FmtUtils.stringForNode(result.asNode(), model));
			}
			catch(ExprEvalException ex) {
				sb.append(" : ");
				sb.append(ex.getLocalizedMessage());
				throw ex;
			}
			finally {
				long endTime = System.currentTimeMillis();
				SPINStatistics stats = new SPINStatistics(sb.toString(), queryString, endTime - startTime, startTime, NodeFactory.createURI(uri));
				SPINStatisticsManager.get().addSilently(Collections.singleton(stats));
			}
			return result;
		}
		else {
			if(cachable) {
				return SHACLFunctionsCache.get().execute(this, dataset, model, bindings, argsForCache);
			}
			else {
				return executeBody(dataset, model, bindings);
			}
		}
	}


	public NodeValue executeBody(Model model, QuerySolution bindings) {
		return executeBody(null, model, bindings);
	}
	
	
	public NodeValue executeBody(Dataset dataset, Model defaultModel, QuerySolution bindings) {
		QueryExecution qexec;
		if(dataset != null) {
			Dataset newDataset = new DatasetWithDifferentDefaultModel(defaultModel, dataset);
			qexec = ARQFactory.get().createQueryExecution(arqQuery, newDataset);
		}
		else {
			qexec = ARQFactory.get().createQueryExecution(arqQuery, defaultModel);
		}
		qexec.setInitialBinding(bindings);
		if(arqQuery.isAskType()) {
			boolean result = qexec.execAsk();
			qexec.close();
			return NodeValue.makeBoolean(result);
		}
		else if(arqQuery.isSelectType()) {
			ResultSet rs = qexec.execSelect();
			try {
				if(rs.hasNext()) {
					QuerySolution s = rs.nextSolution();
					List<String> resultVars = rs.getResultVars();
					String varName = resultVars.get(0);
					RDFNode resultNode = s.get(varName);
					if(resultNode != null) {
						return NodeValue.makeNode(resultNode.asNode());
					}
				}
				throw new ExprEvalException("Empty result set for SHACL function");
			}
			finally {
				qexec.close();
			}
		}
		else {
			throw new ExprEvalException("Body must be ASK or SELECT query");
		}
	}
	
	
	/**
	 * Gets the names of the declared arguments, in order from left to right.
	 * @return the argument names
	 */
	public String[] getArgNames() {
		return argNames.toArray(new String[0]);
	}
	
	
	/**
	 * Gets the properties of the declared arguments, in order from left to right.
	 * @return the argument property Nodes
	 */
	public Node[] getArgPropertyNodes() {
		return argNodes.toArray(new Node[0]);
	}
	

	/**
	 * Gets the Jena Query object for execution.
	 * @return the Jena Query
	 */
	public com.hp.hpl.jena.query.Query getBodyQuery() {
		return arqQuery;
	}
	
	
	/**
	 * Gets the underlying SHACLFunction Model object for this ARQ function.
	 * @return the SHACLFunction
	 */
	public SHACLFunction getSHACLFunction() {
		return shaclFunction;
	}
}
