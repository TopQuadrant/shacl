package org.topbraid.shacl.validation.js;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
import org.topbraid.shacl.targets.Target;
import org.topbraid.shacl.vocabulary.SH;

public class JSTarget implements Target {

	private SHJSExecutable as;
	
	private SHParameterizableTarget parameterizableTarget;
	
	
	JSTarget(Resource executable, SHParameterizableTarget parameterizableTarget) {
		if(parameterizableTarget != null) {
			as = parameterizableTarget.getParameterizable().as(SHJSExecutable.class);
		}
		else {
			as = executable.as(SHJSExecutable.class);
		}
	}
	

	@Override
	public void addTargetNodes(Dataset dataset, Collection<RDFNode> results) {
		
		boolean nested = SHACLScriptEngineManager.begin();
		JSScriptEngine engine = SHACLScriptEngineManager.getCurrentEngine();

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
				for(Object obj : NashornUtil.asArray(result)) {
					Node node = JSFactory.getNode(obj);
					results.add(model.asRDFNode(node));
				}
			}
		}
		catch(Exception ex) {
			ExceptionUtil.throwUnchecked(ex);
		}
		finally {
			dataJSGraph.close();
			SHACLScriptEngineManager.end(nested);
		}
	}

	
	@Override
	public boolean contains(Dataset dataset, RDFNode node) {
		Set<RDFNode> set = new HashSet<>();
		addTargetNodes(dataset, set);
		return set.contains(node);
	}
}
