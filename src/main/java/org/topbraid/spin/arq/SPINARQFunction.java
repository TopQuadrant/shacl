/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.arq;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;
import org.topbraid.spin.model.Argument;
import org.topbraid.spin.model.Function;
import org.topbraid.spin.model.Query;
import org.topbraid.spin.statistics.SPINStatistics;
import org.topbraid.spin.statistics.SPINStatisticsManager;
import org.topbraid.spin.system.SPINArgumentChecker;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SPIN;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.FmtUtils;


/**
 * An ARQ function that delegates its functionality into a user-defined
 * SPIN function. 
 * 
 * @author Holger Knublauch
 */
public class SPINARQFunction implements org.apache.jena.sparql.function.Function, SPINFunctionFactory {
	
	private org.apache.jena.query.Query arqQuery;
	
	private List<String> argNames = new ArrayList<String>();
	
	private List<Node> argNodes = new ArrayList<Node>();
	
	private boolean cachable;
	
	private String queryString;
	
	private Function spinFunction;
	

	/**
	 * Constructs a new SPINARQFunction based on a given SPIN Function.
	 * The spinFunction model be associated with the Model containing
	 * the triples of its definition.
	 * @param spinFunction  the SPIN function
	 */
	public SPINARQFunction(Function spinFunction) {
		
		this.spinFunction = spinFunction;
		
		this.cachable = spinFunction.hasProperty(SPIN.cachable, JenaDatatypes.TRUE);
		
		try {
			Query spinQuery = (Query) spinFunction.getBody();
			queryString = ARQFactory.get().createCommandString(spinQuery);
			arqQuery = ARQFactory.get().createQuery(queryString);
			
			// TODO if above three lines never involve writes, then we can move the optimization up 
			// and the finally block onto the outer try, which would be easier to read.
			JenaUtil.setGraphReadOptimization(true);
			try {
				for(Argument arg : spinFunction.getArguments(true)) {
					String varName = arg.getVarName();
					if(varName == null) {
						throw new IllegalStateException("Argument " + arg + " of " + spinFunction + " does not have a valid predicate");
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
			throw new IllegalArgumentException("Function " + spinFunction.getURI() + " does not define a valid body", ex);
		}
	}
	

	public void build(String uri, ExprList args) {
	}

	
	public org.apache.jena.sparql.function.Function create(String uri) {
		return this;
	}

	
	public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
		
		Graph activeGraph = env.getActiveGraph();
		Model model = activeGraph != null ? 
				ModelFactory.createModelForGraph(activeGraph) :
				ModelFactory.createDefaultModel();
		
		QuerySolutionMap bindings = new QuerySolutionMap();
		Node t = binding.get(Var.alloc(SPIN.THIS_VAR_NAME));
		if(t != null) {
			bindings.add(SPIN.THIS_VAR_NAME, model.asRDFNode(t));
		}
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
		
		if(SPINArgumentChecker.get() != null) {
			SPINArgumentChecker.get().check(spinFunction, bindings);
		}
		
		
		Dataset dataset = DatasetImpl.wrap(env.getDataset());
		
		if(SPINStatisticsManager.get().isRecording() && SPINStatisticsManager.get().isRecordingSPINFunctions()) {
			StringBuffer sb = new StringBuffer();
			sb.append("SPIN Function ");
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
					result = SPINFunctionsCache.get().execute(this, dataset, model, bindings, argsForCache);
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
				return SPINFunctionsCache.get().execute(this, dataset, model, bindings, argsForCache);
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
				throw new ExprEvalException("Empty result set for SPIN function " + queryString);
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
	 * @return the arguments
	 */
	public String[] getArgNames() {
		return argNames.toArray(new String[0]);
	}
	
	
	public Node[] getArgPropertyNodes() {
		return argNodes.toArray(new Node[0]);
	}
	

	/**
	 * Gets the Jena Query object for execution.
	 * @return the Jena Query
	 */
	public org.apache.jena.query.Query getBodyQuery() {
		return arqQuery;
	}
	
	
	public Function getSPINFunction() {
		return spinFunction;
	}
}
