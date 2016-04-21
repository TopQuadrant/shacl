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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.Substitute;
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
import org.topbraid.shacl.model.SHACLConstraintComponent;
import org.topbraid.shacl.model.SHACLFunction;
import org.topbraid.shacl.model.SHACLParameter;
import org.topbraid.shacl.model.SHACLParameterizable;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.arq.DatasetWithDifferentDefaultModel;
import org.topbraid.spin.arq.SPINFunctionFactory;
import org.topbraid.spin.statistics.SPINStatistics;
import org.topbraid.spin.statistics.SPINStatisticsManager;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;


/**
 * An ARQ function that delegates its functionality into a user-defined SHACL function.
 * 
 * There are two ways of declaring such functions:
 * - as sh:Function (similar to SPIN functions)
 * - from constraint components that point at sh:SPARQLAskValidators
 * This class has two constructors for those two cases.
 * 
 * Use the SHACLFunctions class to install them from their definitions in a Model.
 * 
 * @author Holger Knublauch
 */
public class SHACLARQFunction implements org.apache.jena.sparql.function.Function, SPINFunctionFactory {
	
	private org.apache.jena.query.Query arqQuery;
	
	private boolean cachable;
	
	private List<String> paramNames = new ArrayList<String>();
	
	private String queryString;
	
	private SHACLFunction shaclFunction;
	

	/**
	 * Constructs a new SHACLARQFunction based on a given sh:ConstraintComponent
	 * and a given validator (which must be a value of sh:nodeValidator, sh:propertyValidator etc.
	 * @param component  the constraint component (defining the sh:parameters)
	 * @param askValidator  the sh:SPARQLAskValidator resource
	 */
	public SHACLARQFunction(SHACLConstraintComponent component, Resource askValidator) {
		
		try {
			queryString = JenaUtil.getStringProperty(askValidator, SH.sparql);
			arqQuery = ARQFactory.get().createQuery(askValidator.getModel(), queryString);
		}
		catch(Exception ex) {
			throw new IllegalArgumentException("Function " + shaclFunction.getURI() + " does not define a valid body", ex);
		}
		if(!arqQuery.isAskType()) {
            throw new ExprEvalException("Body must be ASK query");
		}
		
		paramNames.add("value");
		addParameters(component);
		paramNames.add("shapesGraph");
	}
	

	/**
	 * Constructs a new SHACLARQFunction based on a given sh:Function.
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
		}
		catch(Exception ex) {
			throw new IllegalArgumentException("Function " + shaclFunction.getURI() + " does not define a valid body", ex);
		}
		if(!arqQuery.isAskType() && !arqQuery.isSelectType()) {
            throw new ExprEvalException("Body must be ASK or SELECT query");
		}

		addParameters(shaclFunction);
	}


	private void addParameters(SHACLParameterizable parameterizable) {
		JenaUtil.setGraphReadOptimization(true);
		try {
			for(SHACLParameter param : parameterizable.getOrderedParameters()) {
				String varName = param.getVarName();
				if(varName == null) {
					throw new IllegalStateException("Parameter " + param + " of " + parameterizable + " does not have a valid predicate");
				}
				paramNames.add(varName);
			}
		}
		finally {
			JenaUtil.setGraphReadOptimization(false);
		}
	}
	

	@Override
    public void build(String uri, ExprList args) {
	}

	
	@Override
    public org.apache.jena.sparql.function.Function create(String uri) {
		return this;
	}

	
	@Override
    public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
		
		Graph activeGraph = env.getActiveGraph();
		Model model = activeGraph != null ? 
				ModelFactory.createModelForGraph(activeGraph) :
				ModelFactory.createDefaultModel();
		
		QuerySolutionMap bindings = new QuerySolutionMap();
		
		Node[] paramsForCache;
		if(cachable) {
			paramsForCache = new Node[args.size()];
		}
		else {
			paramsForCache = null;
		}
		for(int i = 0; i < args.size(); i++) {
			Expr expr = args.get(i);
			if(expr != null && (!expr.isVariable() || binding.contains(expr.asVar()))) {
	        	NodeValue x = expr.eval(binding, env);
	        	if(x != null) {
	        		String paramName;
	        		if(i < paramNames.size()) {
	        			paramName = paramNames.get(i);
	        		}
	        		else {
	        			paramName = "arg" + (i + 1);
	        		}
	        		bindings.add(paramName, model.asRDFNode(x.asNode()));
	        		if(cachable) {
	        			paramsForCache[i] = x.asNode();
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
					result = SHACLFunctionsCache.get().execute(this, dataset, model, bindings, paramsForCache);
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
				return SHACLFunctionsCache.get().execute(this, dataset, model, bindings, paramsForCache);
			}
			else {
				return executeBody(dataset, model, bindings);
			}
		}
	}


	public NodeValue executeBody(Model model, QuerySolution bindings) {
		return executeBody(null, model, bindings);
	}
	
	
	private QueryExecution createQueryExecution(Dataset dataset, Model defaultModel, QuerySolution bindings) {
	    if(dataset == null) {
            return ARQFactory.get().createQueryExecution(arqQuery, defaultModel, bindings);
	    }
	    else {
	    	Dataset newDataset = new DatasetWithDifferentDefaultModel(defaultModel, dataset);
	    	return ARQFactory.get().createQueryExecution(arqQuery, newDataset, bindings);
	    }
	}
	
	
	public NodeValue executeBody(Dataset dataset, Model defaultModel, QuerySolution bindings) {
	    try( QueryExecution qexec = createQueryExecution(dataset, defaultModel, bindings) ) {
	        if(arqQuery.isAskType()) {
	            boolean result = qexec.execAsk();
	            return NodeValue.makeBoolean(result);
	        }
	        else {
	            ResultSet rs = qexec.execSelect();
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
	    }
	}
	

	/**
	 * Gets the Jena Query object for execution.
	 * @return the Jena Query
	 */
	public org.apache.jena.query.Query getBodyQuery() {
		return arqQuery;
	}
	
	
	/**
	 * Gets the underlying SHACLFunction Model object for this ARQ function.
	 * @return the SHACLFunction (may be null)
	 */
	public SHACLFunction getSHACLFunction() {
		return shaclFunction;
	}
	
	
	/**
	 * Gets the names of the declared parameters, in order from left to right.
	 * @return the parameter names
	 */
	public String[] getParamNames() {
		return paramNames.toArray(new String[0]);
	}
}
