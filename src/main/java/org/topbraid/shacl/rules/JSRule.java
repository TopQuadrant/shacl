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
package org.topbraid.shacl.rules;

import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.jenax.progress.ProgressMonitor;
import org.topbraid.jenax.util.ExceptionUtil;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.engine.Shape;
import org.topbraid.shacl.js.JSGraph;
import org.topbraid.shacl.js.JSScriptEngine;
import org.topbraid.shacl.js.NashornUtil;
import org.topbraid.shacl.js.SHACLScriptEngineManager;
import org.topbraid.shacl.js.model.JSFactory;
import org.topbraid.shacl.model.SHJSExecutable;
import org.topbraid.shacl.validation.SHACLException;
import org.topbraid.shacl.vocabulary.SH;

class JSRule extends AbstractRule {
	
	
	JSRule(Resource rule) {
		super(rule);
	}
	

	@Override
	public void execute(RuleEngine ruleEngine, List<RDFNode> focusNodes, Shape shape) {

		Resource rule = getResource();
		String functionName = JenaUtil.getStringProperty(rule, SH.jsFunctionName);
		if(functionName == null) {
			throw new IllegalArgumentException("Missing JavaScript function name at rule " + rule);
		}
		
		ProgressMonitor monitor = ruleEngine.getProgressMonitor();
		for(RDFNode focusNode : focusNodes) {
			
			if(monitor != null && monitor.isCanceled()) {
				return;
			}
			
			boolean nested = SHACLScriptEngineManager.begin();
			JSScriptEngine engine = SHACLScriptEngineManager.getCurrentEngine();
	
			SHJSExecutable as = rule.as(SHJSExecutable.class);
			JSGraph dataJSGraph = new JSGraph(ruleEngine.getDataset().getDefaultModel().getGraph(), engine);
			JSGraph shapesJSGraph = new JSGraph(ruleEngine.getDataset().getDefaultModel().getGraph(), engine);
			try {
				engine.executeLibraries(as);
				engine.put(SH.JS_DATA_VAR, dataJSGraph);
				engine.put(SH.JS_SHAPES_VAR, shapesJSGraph);
				
				QuerySolutionMap bindings = new QuerySolutionMap();
				bindings.add(SH.thisVar.getVarName(), focusNode);
				Object result = engine.invokeFunction(functionName, bindings);
				if(NashornUtil.isArray(result)) {
					for(Object tripleO : NashornUtil.asArray(result)) {
						if(NashornUtil.isArray(tripleO)) {
							Object[] nodes = NashornUtil.asArray(tripleO);
							Node subject = JSFactory.getNode(nodes[0]);
							Node predicate = JSFactory.getNode(nodes[1]);
							Node object = JSFactory.getNode(nodes[2]);
							ruleEngine.infer(Triple.create(subject, predicate, object), this, shape);
						}
						else if(tripleO instanceof Map) {
							@SuppressWarnings("rawtypes")
							Map triple = (Map) tripleO;
							Node subject = JSFactory.getNode(triple.get("subject"));
							Node predicate = JSFactory.getNode(triple.get("predicate"));
							Node object = JSFactory.getNode(triple.get("object"));
							ruleEngine.infer(Triple.create(subject, predicate, object), this, shape);
						}
						else {
							throw new SHACLException("Array members produced by rule must be either arrays with three nodes, or JS objects with subject, predicate and object");
						}
					}
				}
			}
			catch(ScriptException ex) {
				ExceptionUtil.throwUnchecked(ex);
			}
			catch(Exception ex) {
				ex.printStackTrace();
				throw new ExprEvalException(ex);
			}
			finally {
				dataJSGraph.close();
				SHACLScriptEngineManager.end(nested);
			}
		}
	}
	
	
	@Override
    public String toString() {
		String label = JenaUtil.getStringProperty(getResource(), RDFS.label);
		if(label == null) {
			Statement s = getResource().getProperty(SH.jsFunctionName);
			if(s != null && s.getObject().isLiteral()) {
				label = s.getString();
			}
			else {
				label = "(Missing JavaScript function name)";
			}
		}
		return getLabelStart("JavaScript") + label;
	}
}
