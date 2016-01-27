/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.arq;

import java.util.Collections;

import org.topbraid.spin.statistics.SPINStatistics;
import org.topbraid.spin.statistics.SPINStatisticsManager;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.FmtUtils;


/**
 * Base implementation of Function comparable to Jena's FunctionBase.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractFunction implements Function {

	public void build(String uri, ExprList args) {
	}

	
	public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
		Node[] nodes = new Node[args.size()];
		for(int i = 0; i < args.size(); i++) {
            Expr e = args.get(i);
            try {
            	if(e != null && (!e.isVariable() || (e.isVariable() && binding.get(e.asVar()) != null)) ) {
	            	NodeValue x = e.eval(binding, env);
	            	if (x != null) {
						nodes[i] = x.asNode();
					} 
            	}
            }
            catch(ExprEvalException ex) {
            	throw ex;
            }
            catch(Exception ex) {
            	throw new IllegalArgumentException("Error during function evaluation", ex);
            }
        }
		if(SPINStatisticsManager.get().isRecording() && SPINStatisticsManager.get().isRecordingNativeFunctions()) {
			StringBuffer sb = new StringBuffer();
			sb.append("SPARQL Function ");
			sb.append(SSE.format(NodeFactory.createURI(uri), env.getActiveGraph().getPrefixMapping()));
			sb.append("(");
			for(int i = 0; i < nodes.length; i++) {
				if(i > 0) {
					sb.append(", ");
				}
				if(nodes[i] == null) {
					sb.append("?arg" + (i + 1));
				}
				else {
					sb.append(SSE.format(nodes[i], env.getActiveGraph().getPrefixMapping()));
				}
			}
			sb.append(")");
			long startTime = System.currentTimeMillis();
			NodeValue result;
			try {
				result = exec(nodes, env);
				sb.append(" = ");
				sb.append(FmtUtils.stringForNode(result.asNode(), env.getActiveGraph().getPrefixMapping()));
			}
			catch(ExprEvalException ex) {
				sb.append(" : ");
				sb.append(ex.getLocalizedMessage());
				throw ex;
			}
			finally {
				long endTime = System.currentTimeMillis();
				SPINStatistics stats = new SPINStatistics(sb.toString(), 
						"(Native built-in function)", endTime - startTime, startTime, NodeFactory.createURI(uri));
				SPINStatisticsManager.get().addSilently(Collections.singleton(stats));
			}
			return result;
		}
		else {
			return exec(nodes, env);
		}
	}
	
	
	protected abstract NodeValue exec(Node[] nodes, FunctionEnv env);
}
