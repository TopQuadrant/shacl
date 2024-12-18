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

package org.topbraid.jenax.functions;

import java.util.Collections;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.FmtUtils;
import org.topbraid.jenax.statistics.ExecStatistics;
import org.topbraid.jenax.statistics.ExecStatisticsManager;


/**
 * Base implementation of Function comparable to Jena's FunctionBase.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractFunction implements Function {

	@Override
	public void build(String uri, ExprList args, Context context){}

	@Override
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
		if(ExecStatisticsManager.get().isRecording() && ExecStatisticsManager.get().isRecordingNativeFunctions()) {
			StringBuffer sb = new StringBuffer();
			sb.append("SPARQL Function ");
			PrefixMapping pm = env.getActiveGraph() != null ? env.getActiveGraph().getPrefixMapping() : new PrefixMappingImpl();
			sb.append(SSE.str(NodeFactory.createURI(uri), pm));
			sb.append("(");
			for(int i = 0; i < nodes.length; i++) {
				if(i > 0) {
					sb.append(", ");
				}
				if(nodes[i] == null) {
					sb.append("?arg" + (i + 1));
				}
				else {
					sb.append(SSE.str(nodes[i], pm));
				}
			}
			sb.append(")");
			long startTime = System.currentTimeMillis();
			NodeValue result;
			try {
				result = exec(nodes, env);
				sb.append(" = ");
				sb.append(FmtUtils.stringForNode(result.asNode(), pm));
			}
			catch(ExprEvalException ex) {
				sb.append(" : ");
				sb.append(ex.getLocalizedMessage());
				throw ex;
			}
			finally {
				long endTime = System.currentTimeMillis();
				ExecStatistics stats = new ExecStatistics(sb.toString(), 
						"(Native built-in function)", endTime - startTime, startTime, NodeFactory.createURI(uri));
				ExecStatisticsManager.get().addSilently(Collections.singleton(stats));
			}
			return result;
		}
		else {
			return exec(nodes, env);
		}
	}
	
	
	protected abstract NodeValue exec(Node[] nodes, FunctionEnv env);
}
