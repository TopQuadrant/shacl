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

import java.net.URI;
import java.util.Collections;
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
import org.topbraid.jenax.statistics.ExecStatistics;
import org.topbraid.jenax.statistics.ExecStatisticsManager;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.js.JSGraph;
import org.topbraid.shacl.js.JSScriptEngine;
import org.topbraid.shacl.js.NashornUtil;
import org.topbraid.shacl.js.SHACLScriptEngineManager;
import org.topbraid.shacl.js.model.JSFactory;
import org.topbraid.shacl.js.model.JSTerm;
import org.topbraid.shacl.model.SHJSExecutable;
import org.topbraid.shacl.util.FailureLog;
import org.topbraid.shacl.validation.ConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

public abstract class AbstractJSExecutor implements ConstraintExecutor {
	
	protected final static String SHACL = "SHACL";

	
	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine validationEngine, List<RDFNode> focusNodes) {
		
		JSScriptEngine jsEngine = SHACLScriptEngineManager.getCurrentEngine();
		
		Dataset dataset = validationEngine.getDataset();
		URI shapesGraphURI = validationEngine.getShapesGraphURI();
		String functionName = null;
		JSGraph shapesJSGraph = new JSGraph(dataset.getNamedModel(shapesGraphURI.toString()).getGraph(), jsEngine);
		Model dataModel = dataset.getDefaultModel();
		Object oldSHACL = jsEngine.get(SHACL);
		jsEngine.put(SHACL, new SHACLObject(shapesGraphURI, dataset));
		JSGraph dataJSGraph = new JSGraph(dataModel.getGraph(), jsEngine);
		try {
			
			jsEngine.put(SH.JS_SHAPES_VAR, shapesJSGraph);
			jsEngine.put(SH.JS_DATA_VAR, dataJSGraph);
			
			QuerySolutionMap bindings = new QuerySolutionMap();
			bindings.add(SH.currentShapeVar.getName(), constraint.getShapeResource());
			addBindings(constraint, bindings);

			SHJSExecutable executable = getExecutable(constraint);
			functionName = executable.getFunctionName();
			jsEngine.executeLibraries(executable);
			
			long startTime = System.currentTimeMillis();
			for(RDFNode theFocusNode : focusNodes) {
				Object resultObj;
				bindings.add(SH.thisVar.getVarName(), theFocusNode);
				
				List<RDFNode> valueNodes = getValueNodes(validationEngine, constraint, bindings, theFocusNode);
				
				for(RDFNode valueNode : valueNodes) {
					bindings.add("value", valueNode);
					resultObj = jsEngine.invokeFunction(functionName, bindings);
					handleJSResultObject(resultObj, validationEngine, constraint, theFocusNode, valueNode, executable, bindings);
				}
			}
			if(ExecStatisticsManager.get().isRecording()) {
				long endTime = System.currentTimeMillis();
				long duration = endTime - startTime;
				String label = getLabel(constraint);
				ExecStatistics stats = new ExecStatistics(label, null, duration, startTime, constraint.getComponent().asNode());
				ExecStatisticsManager.get().add(Collections.singletonList(stats));
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			Resource result = validationEngine.createResult(DASH.FailureResult, constraint, null);
			result.addProperty(SH.resultMessage, "Could not execute JavaScript constraint");
			if(SH.JSConstraintComponent.equals(constraint.getComponent())) {
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
	private void addDefaultMessages(ValidationEngine engine, Constraint constraint, Resource messageHolder, Resource fallback, Resource result, 
				QuerySolution bindings, Map resultObject) {
		if(constraint != null && constraint.getShapeResource().hasProperty(SH.message)) {
			for(Statement s : constraint.getShapeResource().listProperties(SH.message).toList()) {
				result.addProperty(SH.resultMessage, s.getObject());
			}
		}
		else {
	
			boolean found = false;
			for(Statement s : messageHolder.listProperties(SH.message).toList()) {
				if(s.getObject().isLiteral()) {
					QuerySolutionMap map = new QuerySolutionMap();
					map.addAll(bindings);
					if(resultObject != null) {
						for(Object keyObject : resultObject.keySet()) {
							String key = (String) keyObject;
							Object value = resultObject.get(key);
							if(value != null) {
								Node valueNode = JSFactory.getNode(value);
								if(valueNode != null) {
									map.add(key, result.getModel().asRDFNode(valueNode));
								}
							}
						}
					}
					engine.addResultMessage(result, s.getLiteral(), map);
					found = true;
				}
			}
			if(!found && fallback != null) {
				addDefaultMessages(engine, null, fallback, null, result, bindings, resultObject);
			}
		}
	}


	private Resource createValidationResult(ValidationEngine engine, Constraint constraint, RDFNode focusNode) {
		Resource result = engine.createResult(SH.ValidationResult, constraint, focusNode);
		if(SH.JSConstraintComponent.equals(constraint.getComponent())) {
			result.addProperty(SH.sourceConstraint, constraint.getParameterValue());
		}
		Resource path = JenaUtil.getResourceProperty(constraint.getShapeResource(), SH.path);
		if(path != null) {
			result.addProperty(SH.resultPath, SHACLPaths.clonePath(path, result.getModel()));
		}
		return result;
	}
	
	
	private void handleJSResultObject(Object resultObj, ValidationEngine engine, Constraint constraint, 
			RDFNode focusNode, RDFNode valueNode, Resource messageHolder, QuerySolution bindings) throws Exception {
		if(NashornUtil.isArray(resultObj)) {
			for(Object ro : NashornUtil.asArray(resultObj)) {
				createValidationResultFromJSObject(engine, constraint, focusNode, messageHolder, bindings, ro);
			}
		}
		else if(resultObj instanceof Map) {
			createValidationResultFromJSObject(engine, constraint, focusNode, messageHolder, bindings, resultObj);
		}
		else if(resultObj instanceof Boolean) {
			if(!(Boolean)resultObj) {
				Resource result = createValidationResult(engine, constraint, focusNode);
				if(valueNode != null) {
					result.addProperty(SH.value, valueNode);
				}
				addDefaultMessages(engine, constraint, messageHolder, constraint.getComponent(), result, bindings, null);
			}
		}
		else if(resultObj instanceof String) {
			Resource result = createValidationResult(engine, constraint, focusNode);
			result.addProperty(SH.resultMessage, (String)resultObj);
			if(valueNode != null) {
				result.addProperty(SH.value, valueNode);
			}
			addDefaultMessages(engine, constraint, messageHolder, constraint.getComponent(), result, bindings, null);
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	private void createValidationResultFromJSObject(ValidationEngine engine, Constraint constraint, RDFNode focusNode,
			Resource messageHolder, QuerySolution bindings, Object ro) {
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
			addDefaultMessages(engine, constraint, messageHolder, constraint.getComponent(), result, bindings, ro instanceof Map ? (Map)ro : null);
		}
	}
	
	
	protected abstract String getLabel(Constraint constraint);
}
