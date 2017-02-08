package org.topbraid.shacl.js;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
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
	
	private final static String SHACL = "SHACL";
	
	private static JSExecutionLanguage singleton = new JSExecutionLanguage();
	
	public static JSExecutionLanguage get() {
		return singleton;
	}

	
	@Override
	public SHConstraint asConstraint(Resource c) {
		return c.as(SHJSConstraint.class);
	}


	@Override
	public boolean canExecuteConstraint(ConstraintExecutable executable) {
		if(executable instanceof JSConstraintExecutable) {
			return executable.getConstraint().hasProperty(SHJS.jsFunctionName);
		}
		else if(executable instanceof ComponentConstraintExecutable) {
			ComponentConstraintExecutable cce = (ComponentConstraintExecutable)executable;
			Resource validator = cce.getValidator(SHJS.JSValidator);
			if(validator != null && (validator.hasProperty(SHJS.jsFunctionName))) {
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

		JSScriptEngine engine = SHACLScriptEngineManager.getCurrentEngine();
		
		String functionName = null;
		JSGraph shapesJSGraph = new JSGraph(dataset.getNamedModel(shapesGraphURI.toString()).getGraph());
		Model dataModel = dataset.getDefaultModel();
		Object oldSHACL = engine.get(SHACL);
		engine.put(SHACL, new SHACLObject(shapesGraphURI, dataset));
		JSGraph dataJSGraph = new JSGraph(dataModel.getGraph());
		try {
			
			SHJSExecutable as = executable.getConstraint().as(SHJSExecutable.class);
			engine.executeLibraries(as);
			
			QuerySolutionMap bindings = new QuerySolutionMap();
			if(shape != null) {
				bindings.add("currentShape", shape);
			}
			
			engine.put("$shapesGraph", shapesJSGraph);
			engine.put("$dataGraph", dataJSGraph);
			
			if(executable instanceof ComponentConstraintExecutable) {
				((ComponentConstraintExecutable)executable).addBindings(bindings);
			}

			Resource validator = null;
			if(executable instanceof JSConstraintExecutable) {
				SHJSConstraint jsc = (SHJSConstraint) executable.getConstraint();
				functionName = jsc.getFunctionName();
			}
			else {
				validator = ((ComponentConstraintExecutable)executable).getValidator(getExecutableType());
				functionName = JenaUtil.getStringProperty(validator, SHJS.jsFunctionName);
				engine.executeLibraries(validator);
			}
			
			boolean returnResult = false;
			
			for(RDFNode theFocusNode : focusNodes) {
				Object resultObj;
				bindings.add("focusNode", theFocusNode);
				
				List<RDFNode> valueNodes = new LinkedList<>();
				if(validator != null) {
					Resource component = ((ComponentConstraintExecutable)executable).getComponent();
					Resource context = ((ComponentConstraintExecutable)executable).getContext();
					if(SH.PropertyShape.equals(context)) {
						if(component.hasProperty(SH.propertyValidator, validator)) {
							bindings.add("path", executable.getConstraint().getRequiredProperty(SH.path).getObject());
							valueNodes.add(null);
						}
						else if(!theFocusNode.isLiteral()) {
							// TODO: Support paths
							Property path = JenaUtil.asProperty(executable.getConstraint().getPropertyResourceValue(SH.path));
							StmtIterator it = theFocusNode.getModel().listStatements((Resource)theFocusNode, path, (RDFNode)null);
							while(it.hasNext()) {
								valueNodes.add(it.next().getObject());
							}
						}
					}
					else if(SH.NodeShape.equals(context)) {
						bindings.add("value", theFocusNode);
						valueNodes.add(theFocusNode);
					}
				}
				else {
					valueNodes.add(theFocusNode);
				}
				
				for(RDFNode valueNode : valueNodes) {
					
					bindings.add("value", valueNode);
					
					resultObj = engine.invokeFunction(functionName, bindings);
					
					if(NashornUtil.isArray(resultObj)) {
						for(Object ro : NashornUtil.asArray(resultObj)) {
							Resource result = createValidationResult(report, shape, executable, theFocusNode);
							if(ro instanceof Map) {
								Object value = ((Map)ro).get("value");
								if(value instanceof JSTerm) {
									Node resultValueNode = JSFactory.getNode(value);
									if(resultValueNode != null) {
										result.addProperty(SH.value, dataModel.asRDFNode(resultValueNode));
									}
								}
							}
							returnResult = true;
						}
					}
					else if(resultObj instanceof Boolean) {
						if(!(Boolean)resultObj) {
							Resource result = createValidationResult(report, shape, executable, theFocusNode);
							if(valueNode != null) {
								result.addProperty(SH.value, valueNode);
							}
							resultsList.add(result);
							returnResult = true;
						}
					}
					else if(resultObj instanceof String) {
						Resource result = createValidationResult(report, shape, executable, theFocusNode);
						result.addProperty(SH.resultMessage, (String)resultObj);
						if(valueNode != null) {
							result.addProperty(SH.value, valueNode);
						}
						resultsList.add(result);
						returnResult = true;
					}
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
			FailureLog.get().logFailure("Could not execute JavaScript function \"" + functionName + "\": " + ex);
			return true;
		}
		finally {
			dataJSGraph.close();
			shapesJSGraph.close();
			engine.put(SHACL, oldSHACL);
		}
	}


	private Resource createValidationResult(Resource report, Resource shape, ConstraintExecutable executable,
			RDFNode focusNode) {
		Resource result = report.getModel().createResource(SH.ValidationResult);
		report.addProperty(SH.result, result);
		result.addProperty(SH.resultSeverity, SH.Violation); // TODO: Generalize
		result.addProperty(SH.sourceConstraint, executable.getConstraint());
		if(executable instanceof ComponentConstraintExecutable) {
			result.addProperty(SH.sourceConstraintComponent, ((ComponentConstraintExecutable)executable).getComponent());
		}
		else {	
			result.addProperty(SH.sourceConstraintComponent, SHJS.JSConstraintComponent);
		}
		result.addProperty(SH.sourceShape, shape);
		result.addProperty(SH.focusNode, focusNode);
		Resource path = JenaUtil.getResourceProperty(executable.getConstraint(), SH.path);
		if(path != null) {
			result.addProperty(SH.resultPath, path);
		}
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
