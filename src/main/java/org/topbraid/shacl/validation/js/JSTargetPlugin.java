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
package org.topbraid.shacl.validation.js;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.util.ExceptionUtil;
import org.topbraid.shacl.js.JSGraph;
import org.topbraid.shacl.js.JSScriptEngine;
import org.topbraid.shacl.js.NashornUtil;
import org.topbraid.shacl.js.SHACLScriptEngineManager;
import org.topbraid.shacl.js.model.JSFactory;
import org.topbraid.shacl.model.SHJSExecutable;
import org.topbraid.shacl.model.SHParameterizableTarget;
import org.topbraid.shacl.validation.TargetPlugin;
import org.topbraid.shacl.vocabulary.SH;

public class JSTargetPlugin implements TargetPlugin {

	@Override
	public boolean canExecuteTarget(Resource target) {
		return target.hasProperty(SH.jsFunctionName);
	}

	
	@Override
	public Iterable<RDFNode> executeTarget(Dataset dataset, Resource target,
			SHParameterizableTarget parameterizableTarget) {
		
		boolean nested = SHACLScriptEngineManager.begin();
		JSScriptEngine engine = SHACLScriptEngineManager.getCurrentEngine();

		SHJSExecutable as;
		if(parameterizableTarget != null) {
			as = parameterizableTarget.getParameterizable().as(SHJSExecutable.class);
		}
		else {
			as = target.as(SHJSExecutable.class);
		}
		Model model = dataset.getDefaultModel();
		JSGraph dataJSGraph = new JSGraph(model.getGraph(), engine);
		try {
			engine.executeLibraries(as);
			engine.put(SH.JS_DATA_VAR, dataJSGraph);
			
			QuerySolutionMap bindings = new QuerySolutionMap();
			if(parameterizableTarget != null) {
				parameterizableTarget.addBindings(bindings);
			}

			Object result = engine.invokeFunction(as.getFunctionName(), bindings);
			if(NashornUtil.isArray(result)) {
				List<RDFNode> results = new LinkedList<RDFNode>();
				for(Object obj : NashornUtil.asArray(result)) {
					Node node = JSFactory.getNode(obj);
					results.add(model.asRDFNode(node));
				}
				return results;
			}
		}
		catch(Exception ex) {
			ExceptionUtil.throwUnchecked(ex);
		}
		finally {
			dataJSGraph.close();
			SHACLScriptEngineManager.end(nested);
		}
		return Collections.emptyList();
	}


	@Override
	public boolean isNodeInTarget(RDFNode focusNode, Dataset dataset, Resource executable, SHParameterizableTarget parameterizableTarget) {
		for(RDFNode target : executeTarget(dataset, executable, parameterizableTarget)) {
			if(focusNode.equals(target)) {
				return true;
			}
		}
		return false;
	}
}
