package org.topbraid.shacl.arq;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptEngine;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.topbraid.shacl.js.JSGraph;
import org.topbraid.shacl.js.JSScriptEngines;
import org.topbraid.shacl.js.model.JSFactory;
import org.topbraid.shacl.model.SHJSExecutable;
import org.topbraid.shacl.model.SHJSFunction;
import org.topbraid.spin.util.JenaDatatypes;

public class SHACLJSARQFunction extends SHACLARQFunction {

	public SHACLJSARQFunction(SHJSFunction shaclFunction) {
		super(shaclFunction);
	}

	
	@Override
	public NodeValue executeBody(Dataset dataset, Model dataModel, QuerySolution bindings) {
		
		ScriptEngine engine = JSScriptEngines.get().createScriptEngine();

		Set<Resource> visited = new HashSet<>();
		SHJSExecutable as = getSHACLFunction().as(SHJSExecutable.class);
		JSGraph dataJSGraph = new JSGraph(dataModel.getGraph());
		try {
			JSScriptEngines.get().executeLibraries(engine, as, visited, false);
			engine.put("$dataGraph", dataJSGraph);
			
			Iterator<String> varNames = bindings.varNames();
			while(varNames.hasNext()) {
				String varName = varNames.next();
				RDFNode value = bindings.get(varName);
				if(value != null) {
					engine.put("$" + varName, JSFactory.asJSTerm(value.asNode()));
				}
			}
			
			String functionName = JSScriptEngines.get().installFunction(engine, as.getScript());
			Object result = ((Invocable) engine).invokeFunction(functionName);
			if(result != null) {
				Node node = JSFactory.getNode(result);
				if(node != null) {
					return NodeValue.makeNode(node);
				}
				else if(result instanceof String) {
					return NodeValue.makeNode(NodeFactory.createLiteral((String)result));
				}
				else if(result instanceof Integer) {
					return NodeValue.makeNode(JenaDatatypes.createInteger((Integer)result).asNode());
				}
				else if(result instanceof Double) {
					return NodeValue.makeDecimal((Double)result);
				}
				else if(result instanceof Boolean) {
					return NodeValue.booleanReturn((Boolean)result);
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			throw new ExprEvalException(ex);
		}
		finally {
			dataJSGraph.close();
		}
		throw new ExprEvalException();
	}

	
	@Override
	protected String getQueryString() {
		return ((SHJSFunction)getSHACLFunction()).getScript();
	}
}
