package org.topbraid.shacl.validation.js;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.js.JSGraph;
import org.topbraid.shacl.js.JSScriptEngine;
import org.topbraid.shacl.js.NashornUtil;
import org.topbraid.shacl.js.SHACLScriptEngineManager;
import org.topbraid.shacl.js.model.JSFactory;
import org.topbraid.shacl.js.model.JSTerm;
import org.topbraid.shacl.model.SHJSExecutable;
import org.topbraid.shacl.util.FailureLog;
import org.topbraid.shacl.validation.ConstraintExecutor;
import org.topbraid.shacl.validation.Constraint;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.shacl.vocabulary.SHJS;
import org.topbraid.spin.util.JenaUtil;

public abstract class AbstractJSExecutor implements ConstraintExecutor {
	
	protected final static String SHACL = "SHACL";

	
	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine validationEngine, List<RDFNode> focusNodes) {
		
		JSScriptEngine jsEngine = SHACLScriptEngineManager.getCurrentEngine();
		
		Dataset dataset = validationEngine.getDataset();
		URI shapesGraphURI = validationEngine.getShapesGraphURI();
		String functionName = null;
		JSGraph shapesJSGraph = new JSGraph(dataset.getNamedModel(shapesGraphURI.toString()).getGraph());
		Model dataModel = dataset.getDefaultModel();
		Object oldSHACL = jsEngine.get(SHACL);
		jsEngine.put(SHACL, new SHACLObject(shapesGraphURI, dataset));
		JSGraph dataJSGraph = new JSGraph(dataModel.getGraph());
		try {
			
			jsEngine.put("$shapesGraph", shapesJSGraph);
			jsEngine.put("$dataGraph", dataJSGraph);
			
			QuerySolutionMap bindings = new QuerySolutionMap();
			bindings.add(SH.currentShapeVar.getName(), constraint.getShapeResource());
			addBindings(constraint, bindings);

			SHJSExecutable executable = getExecutable(constraint);
			functionName = executable.getFunctionName();
			jsEngine.executeLibraries(executable);
			
			for(RDFNode theFocusNode : focusNodes) {
				Object resultObj;
				bindings.add("focusNode", theFocusNode);
				
				List<RDFNode> valueNodes = getValueNodes(validationEngine, constraint, bindings, theFocusNode);
				
				for(RDFNode valueNode : valueNodes) {
					bindings.add("value", valueNode);
					resultObj = jsEngine.invokeFunction(functionName, bindings);
					handleJSResultObject(resultObj, validationEngine, constraint, theFocusNode, valueNode, executable, bindings);
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			Resource result = validationEngine.createResult(DASH.FailureResult, constraint, null);
			result.addProperty(SH.resultMessage, "Could not execute JavaScript constraint");
			if(SHJS.JSConstraintComponent.equals(constraint.getComponent())) {
				result.addProperty(SH.sourceConstraint, constraint.getParameterValue());
			}
			FailureLog.get().logFailure("Could not execute JavaScript function \"" + functionName + "\": " + ex);
		}
		finally {
			dataJSGraph.close();
			shapesJSGraph.close();
			jsEngine.put(SHACL, oldSHACL);
		}
	}
	
	
	protected abstract void addBindings(Constraint constraint, QuerySolutionMap bindings);
	
	
	protected abstract SHJSExecutable getExecutable(Constraint constraint);

	
	protected abstract List<RDFNode> getValueNodes(ValidationEngine engine, Constraint constraint, QuerySolutionMap bindings, RDFNode focusNode);

	
	@SuppressWarnings("rawtypes")
	private void addDefaultMessages(ValidationEngine engine, Resource messageHolder, Resource result, 
				QuerySolution bindings, Map resultObject) {
		for(Statement s : messageHolder.listProperties(SH.message).toList()) {
			if(s.getObject().isLiteral()) {
				QuerySolutionMap map = new QuerySolutionMap();
				map.addAll(bindings);
				if(resultObject != null) {
					for(Object keyObject : resultObject.keySet()) {
						String key = (String) keyObject;
						Object value = map.get(key);
						if(value != null) {
							Node valueNode = JSFactory.getNode(value);
							if(valueNode != null) {
								map.add(key, result.getModel().asRDFNode(valueNode));
							}
						}
					}
				}
				engine.addResultMessage(result, s.getLiteral(), map);
			}
		}
	}


	private Resource createValidationResult(ValidationEngine engine, Constraint constraint, RDFNode focusNode) {
		Resource result = engine.createResult(SH.ValidationResult, constraint, focusNode);
		if(SHJS.JSConstraintComponent.equals(constraint.getComponent())) {
			result.addProperty(SH.sourceConstraint, constraint.getParameterValue());
		}
		Resource path = JenaUtil.getResourceProperty(constraint.getShapeResource(), SH.path);
		if(path != null) {
			result.addProperty(SH.resultPath, SHACLPaths.clonePath(path, result.getModel()));
		}
		return result;
	}
	
	
	@SuppressWarnings("rawtypes")
	private void handleJSResultObject(Object resultObj, ValidationEngine engine, Constraint constraint, 
			RDFNode focusNode, RDFNode valueNode, Resource messageHolder, QuerySolution bindings) throws Exception {
		if(NashornUtil.isArray(resultObj)) {
			for(Object ro : NashornUtil.asArray(resultObj)) {
				Resource result = createValidationResult(engine, constraint, focusNode);
				if(ro instanceof Map) {
					Object value = ((Map)ro).get("value");
					if(value instanceof JSTerm) {
						Node resultValueNode = JSFactory.getNode(value);
						if(resultValueNode != null) {
							result.addProperty(SH.value, result.getModel().asRDFNode(resultValueNode));
						}
					}
					Object message = ((Map)ro).get("message");
					if(message instanceof String) {
						result.addProperty(SH.resultMessage, (String)message);
					}
					Object path = ((Map)ro).get("path");
					if(path != null) {
						Node pathNode = JSFactory.getNode(path);
						if(pathNode != null && pathNode.isURI()) {
							result.addProperty(SH.resultPath, result.getModel().asRDFNode(pathNode));
						}
					}
				}
				else if(ro instanceof String) {
					result.addProperty(SH.resultMessage, (String)ro);
				}
				if(!result.hasProperty(SH.resultMessage)) {
					addDefaultMessages(engine, messageHolder, result, bindings, ro instanceof Map ? (Map)ro : null);
				}
			}
		}
		else if(resultObj instanceof Boolean) {
			if(!(Boolean)resultObj) {
				Resource result = createValidationResult(engine, constraint, focusNode);
				if(valueNode != null) {
					result.addProperty(SH.value, valueNode);
				}
				addDefaultMessages(engine, messageHolder, result, bindings, null);
			}
		}
		else if(resultObj instanceof String) {
			Resource result = createValidationResult(engine, constraint, focusNode);
			result.addProperty(SH.resultMessage, (String)resultObj);
			if(valueNode != null) {
				result.addProperty(SH.value, valueNode);
			}
			addDefaultMessages(engine, messageHolder, result, bindings, null);
		}
	}
}
