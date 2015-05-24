/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.arq.functions;

import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.arq.AbstractFunction;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPIN;
import org.topbraid.spin.vocabulary.SPL;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.Function;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.function.FunctionFactory;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.hp.hpl.jena.sparql.util.FmtUtils;

/**
 * The function sh:invoke (and spif:invoke).
 * 
 * @author Holger Knublauch
 */
public class InvokeFunction	extends AbstractFunction {

	@Override
	protected NodeValue exec(Node[] nodes, FunctionEnv env) {
		if(nodes.length == 0) {
			throw new ExprEvalException("Missing function URI argument");
		}
		Node commandNode = nodes[0];
		if(!commandNode.isURI()) {
			throw new ExprEvalException("First argument must be the URI of a function");
		}
		
		String uri = commandNode.getURI();
		
		// Special handling of SPARQL system functions such as sp:gt
		Resource functionResource = SPL.getModel().getResource(uri);
		if(SP.NS.equals(functionResource.getNameSpace())) {
			Statement symbolS = functionResource.getProperty(SPIN.symbol);
			if(symbolS != null) {
				final String varName = "result";
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT ?" + varName + " \n");
				sb.append("WHERE {\n");
				sb.append("    BIND (");
				sb.append(FmtUtils.stringForNode(nodes[1], env.getActiveGraph().getPrefixMapping()));
				sb.append(" ");
				sb.append(symbolS.getString());
				sb.append(" ");
				sb.append(FmtUtils.stringForNode(nodes[2], env.getActiveGraph().getPrefixMapping()));
				sb.append(" AS ?" + varName + ") . \n");
				sb.append("}");
				
				Model model = ModelFactory.createModelForGraph(env.getActiveGraph());
				Query arq = ARQFactory.get().createQuery(model, sb.toString());
				QueryExecution qexec = ARQFactory.get().createQueryExecution(arq, model);
				ResultSet rs = qexec.execSelect();
				try {
					if(rs.hasNext()) {
						RDFNode result = rs.next().get(varName);
						if(result != null) {
							return NodeValue.makeNode(result.asNode());
						}
					}
					throw new ExprEvalException("Failed to evaluate function - empty result set");
				}
				finally {
					qexec.close();
				}
			}
		}
		
		FunctionFactory ff = FunctionRegistry.get().get(uri);
		if(ff == null) {
			throw new ExprEvalException("Unknown function " + uri);
		}
		
		Function function = ff.create(uri);
		ExprList exprList = new ExprList();
		
		for(int i = 1; i < nodes.length; i++) {
			Node node = nodes[i];
			if(node != null) {
				exprList.add(NodeValue.makeNode(node));
			}
			else {
				exprList.add(null);
			}
		}
		
		NodeValue result = function.exec(new BindingHashMap(), exprList, uri, env);
		return result;
	}
}
