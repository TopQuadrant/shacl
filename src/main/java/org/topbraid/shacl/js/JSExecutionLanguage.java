package org.topbraid.shacl.js;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.script.Invocable;
import javax.script.ScriptEngine;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.constraints.ComponentConstraintExecutable;
import org.topbraid.shacl.constraints.ConstraintExecutable;
import org.topbraid.shacl.constraints.ExecutionLanguage;
import org.topbraid.shacl.constraints.FailureLog;
import org.topbraid.shacl.js.model.JSFactory;
import org.topbraid.shacl.js.model.JSTerm;
import org.topbraid.shacl.model.SHConstraint;
import org.topbraid.shacl.model.SHJSConstraint;
import org.topbraid.shacl.model.SHJSExecutable;
import org.topbraid.shacl.model.SHParameterizableTarget;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class JSExecutionLanguage implements ExecutionLanguage {
	
	@Override
	public SHConstraint asConstraint(Resource c) {
		return c.as(SHJSConstraint.class);
	}


	@Override
	public boolean canExecuteConstraint(ConstraintExecutable executable) {
		if(executable instanceof JSConstraintExecutable) {
			return executable.getConstraint().hasProperty(SHJS.script);
		}
		else if(executable instanceof ComponentConstraintExecutable) {
			ComponentConstraintExecutable cce = (ComponentConstraintExecutable)executable;
			Resource validator = cce.getValidator(SHJS.JSValidator);
			if(validator != null && (validator.hasProperty(SHJS.script))) {
				return true;
			}
		}
		return false;
	}

	
	@Override
	public boolean canExecuteTarget(Resource executable) {
		// TODO Auto-generated method stub
		return false;
	}

	
	@SuppressWarnings("rawtypes")
	@Override
	public boolean executeConstraint(Dataset dataset, Resource shape, URI shapesGraphURI,
			ConstraintExecutable executable, RDFNode focusNode, Resource report,
			Function<RDFNode, String> labelFunction, List<Resource> resultsList) {
		
		if(executable.getConstraint().isDeactivated()) {
			return false;
		}
		
		List<RDFNode> focusNodes;
		
		if(focusNode == null) {
			focusNodes = selectFocusNodes(shape, dataset, shapesGraphURI);
			if(focusNodes.isEmpty()) {
				// Bypass everything if set of focus nodes is empty
				return false;
			}
		}
		else {
			focusNodes = Collections.singletonList(focusNode);
		}

		ScriptEngine engine = JSScriptEngines.get().createScriptEngine();
		
		String script = null;
		JSGraph shapesJSGraph = new JSGraph(dataset.getNamedModel(shapesGraphURI.toString()).getGraph());
		Model dataModel = dataset.getDefaultModel();
		JSGraph dataJSGraph = new JSGraph(dataModel.getGraph());
		try {
			
			Set<Resource> visited = new HashSet<>();
			SHJSExecutable as = executable.getConstraint().as(SHJSExecutable.class);
			JSScriptEngines.get().executeLibraries(engine, as, visited, false);
			
			if(shape != null) {
				engine.put("$currentShape", JSFactory.asJSTerm(shape.asNode()));
			}
			
			engine.put("$shapesGraph", shapesJSGraph);
			
			engine.put("$dataGraph", dataJSGraph);
			
			if(executable instanceof ComponentConstraintExecutable) {
				QuerySolutionMap bindings = new QuerySolutionMap();
				((ComponentConstraintExecutable)executable).addBindings(bindings);
				Iterator<String> varNames = bindings.varNames();
				while(varNames.hasNext()) {
					String varName = varNames.next();
					RDFNode value = bindings.get(varName);
					if(value != null) {
						engine.put("$" + varName, JSFactory.asJSTerm(value.asNode()));
					}
				}
			}

			if(executable instanceof JSConstraintExecutable) {
				SHJSConstraint jsc = (SHJSConstraint) executable.getConstraint();
				script = jsc.getScript();
			}
			else {
				Resource validator = ((ComponentConstraintExecutable)executable).getValidator(getExecutableType());
				script = JenaUtil.getStringProperty(validator, SHJS.script);
			}
			
			String functionName = JSScriptEngines.get().installFunction(engine, script);

			boolean returnResult = false;
			
			Invocable invocable = (Invocable) engine;
			for(RDFNode theFocusNode : focusNodes) {
				Object resultObj;
				engine.put("$this", JSFactory.asJSTerm(theFocusNode.asNode()));
				
				// TODO: Generalize for property shapes
				engine.put("$value", JSFactory.asJSTerm(theFocusNode.asNode()));

				resultObj = invocable.invokeFunction(functionName);
				if(NashornUtil.isArray(resultObj)) {
					for(Object ro : NashornUtil.asArray(resultObj)) {
						Resource result = createValidationResult(report, shape, executable, theFocusNode);
						if(ro instanceof Map) {
							Object value = ((Map)ro).get("value");
							if(value instanceof JSTerm) {
								Node valueNode = JSFactory.getNode(value);
								if(valueNode != null) {
									result.addProperty(SH.value, dataModel.asRDFNode(valueNode));
								}
							}
						}
						returnResult = true;
					}
				}
				else if(resultObj instanceof Boolean) {
					if(!(Boolean)resultObj) {
						Resource result = createValidationResult(report, shape, executable, theFocusNode);
						result.addProperty(SH.value, theFocusNode);
						resultsList.add(result);
						returnResult = true;
					}
				}
				else if(resultObj instanceof String) {
					Resource result = createValidationResult(report, shape, executable, theFocusNode);
					result.addProperty(SH.resultMessage, (String)resultObj);
					result.addProperty(SH.value, theFocusNode);
					resultsList.add(result);
					returnResult = true;
				}
			}
			return returnResult;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			Resource result = report.getModel().createResource(DASH.FailureResult);
			report.addProperty(SH.result, result);
			result.addProperty(SH.resultMessage, "Could not execute JavaScript constraint");
			result.addProperty(SH.sourceConstraint, executable.getConstraint());
			result.addProperty(SH.sourceShape, shape);
			if(executable instanceof ComponentConstraintExecutable) {
				result.addProperty(SH.sourceConstraintComponent, ((ComponentConstraintExecutable)executable).getComponent());
			}
			if(focusNode != null) {
				result.addProperty(SH.focusNode, focusNode);
			}
			resultsList.add(result);
			FailureLog.get().logFailure("Could not execute JavaScript constraint \"" + script + "\": " + ex);
			return true;
		}
		finally {
			dataJSGraph.close();
			shapesJSGraph.close();
		}
	}


	private Resource createValidationResult(Resource report, Resource shape, ConstraintExecutable executable,
			RDFNode focusNode) {
		Resource result = report.getModel().createResource(SH.ValidationResult);
		report.addProperty(SH.result, result);
		result.addProperty(SH.resultSeverity, SH.Violation); // TODO: Generalize
		result.addProperty(SH.sourceConstraint, executable.getConstraint());
		result.addProperty(SH.sourceConstraintComponent, SHJS.JSConstraintComponent);
		result.addProperty(SH.sourceShape, shape);
		result.addProperty(SH.focusNode, focusNode);
		return result;
	}

	
	@Override
	public Iterable<RDFNode> executeTarget(Dataset dataset, Resource executable,
			SHParameterizableTarget parameterizableTarget) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Resource getConstraintComponent() {
		return SHJS.JSConstraintComponent;
	}


	@Override
	public Resource getExecutableType() {
		return SHJS.JSValidator;
	}


	@Override
	public Property getParameter() {
		return SHJS.js;
	}

	
	@Override
	public boolean isNodeInTarget(RDFNode focusNode, Dataset dataset, Resource executable,
			SHParameterizableTarget parameterizableTarget) {
		// TODO Auto-generated method stub
		return false;
	}

	
	private List<RDFNode> selectFocusNodes(Resource shape, Dataset dataset, URI shapesGraphURI) {
		Set<RDFNode> results = new HashSet<RDFNode>();
		
		Model dataModel = dataset.getDefaultModel();
		
		if(JenaUtil.hasIndirectType(shape, RDFS.Class)) {
			results.addAll(JenaUtil.getAllInstances(shape.inModel(dataModel)));
		}
		
		for(Resource targetClass : JenaUtil.getResourceProperties(shape, SH.targetClass)) {
			results.addAll(JenaUtil.getAllInstances(targetClass.inModel(dataModel)));
		}
		
		results.addAll(shape.getModel().listObjectsOfProperty(shape, SH.targetNode).toList());
		
		for(Resource sof : JenaUtil.getResourceProperties(shape, SH.targetSubjectsOf)) {
			for(Statement s : dataModel.listStatements(null, JenaUtil.asProperty(sof), (RDFNode)null).toList()) {
				results.add(s.getSubject());
			}
		}
		
		for(Resource sof : JenaUtil.getResourceProperties(shape, SH.targetObjectsOf)) {
			for(Statement s : dataModel.listStatements(null, JenaUtil.asProperty(sof), (RDFNode)null).toList()) {
				results.add(s.getObject());
			}
		}

		return new ArrayList<RDFNode>(results);
	}
}
