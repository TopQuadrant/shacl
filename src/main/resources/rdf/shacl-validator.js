// A simple SHACL validator in JavaScript based on SHACL-JS.

// Design:
//
// First, derive a ShapesGraph object from the definitions in $shapes.
// This manages a map of parameters to ConstraintComponents.
// Each ConstraintComponent manages its list of parameters and a link to the validators.
// 
// The ShapesGraph also manages a list of Shapes, each which has a list of Constraints.
// A Constraint is a specific combination of parameters for a constraint component,
// and has functions to access the target nodes.
//
// Each ShapesGraph can be reused between validation calls, and thus often only needs
// to be created once per application.
//
// The validation process is started by creating a ValidationEngine that relies on
// a given ShapesGraph and operates on the current $data.
// It basically walks through all Shapes that have target nodes and runs the validators
// for each Constraint of the shape, producing results along the way.

TermFactory.registerNamespace("dash", "http://datashapes.org/dash#");


var SHACL = {
		
	depth : 0,
	
	compareNodes : function(node1, node2) {
		// TODO: Does not handle the case where nodes cannot be compared
		return compareTerms(node1, node2);
	},
	
	nodeConformsToShape : function(focusNode, shapeNode) {
		var localEngine = new ValidationEngine(currentShapesGraph, true);
		var shape = currentShapesGraph.getShape(shapeNode);
		try {
			SHACL.depth++;
			return !localEngine.validateNodeAgainstShape(focusNode, shape);
		}
		finally {
			SHACL.depth--;
		}
	}
}


// class Constraint

function Constraint(shape, component, paramValue) {
	this.shape = shape;
	this.component = component;
	this.paramValue = paramValue;
	var parameterValues = {};
	var params = component.getParameters();
	for(var i = 0; i < params.length; i++) {
		var param = params[i];
		var value = paramValue ? paramValue : RDFQuery($shapes).match(shape.shapeNode, param, "?value").getNode("?value");
		if(value) {
			var localName = getLocalName(param.uri);
			parameterValues[localName] = value;
		}
	}
	this.parameterValues = parameterValues;
}

Constraint.prototype.getParameterValue = function(paramName) {
	return this.parameterValues[paramName];
}


// class ConstraintComponent

function ConstraintComponent(node) {
	this.node = node;
	var parameters = [];
	var parameterNodes = [];
	var requiredParameters = [];
	var optionals = {};
	$shapes.query().
		match(node, "sh:parameter", "?parameter").
		match("?parameter", "sh:path", "?path").forEach(function(sol) {
			parameters.push(sol.path);
			parameterNodes.push(sol.parameter);
			if($shapes.query().match(sol.parameter, "sh:optional", "true").hasSolution()) {
				optionals[sol.path.uri] = true;
			}
			else {
				requiredParameters.push(sol.path);
			}
		});
	this.optionals = optionals;
	this.parameters = parameters;
	this.parameterNodes = parameterNodes;
	this.requiredParameters = requiredParameters;
	this.nodeValidationFunction = this.findValidationFunction(T("sh:nodeValidator"));
	if(!this.nodeValidationFunction) {
		this.nodeValidationFunction = this.findValidationFunction(T("sh:validator"));
		this.nodeValidationFunctionGeneric = true;
	}
	this.propertyValidationFunction = this.findValidationFunction(T("sh:propertyValidator"));
	if(!this.propertyValidationFunction) {
		this.propertyValidationFunction = this.findValidationFunction(T("sh:validator"));
		this.propertyValidationFunctionGeneric = true;
	}
}

ConstraintComponent.prototype.findValidationFunction = function(predicate) {
	var functionName = $shapes.query().
		match(this.node, predicate, "?validator").
		match("?validator", "rdf:type", "sh:JSValidator").
		match("?validator", "sh:jsFunctionName", "?functionName").
		getNode("?functionName");
	if(functionName) {
		return new ValidationFunction(functionName.lex, this.parameters);
	}
	else {
		return null;
	}
}

ConstraintComponent.prototype.getParameters = function() {
	return this.parameters;
}

ConstraintComponent.prototype.isComplete = function(shapeNode) {
	for(var i = 0; i < this.parameters.length; i++) {
		var parameter = this.parameters[i];
		if(!this.isOptional(parameter.uri)) {
			if(!RDFQuery($shapes).match(shapeNode, parameter, null).hasSolution()) {
				return false;
			}
		}
	}
	return true;
}

ConstraintComponent.prototype.isOptional = function(parameterURI) {
	return this.optionals[parameterURI];
}


// class Shape

function Shape(shapesGraph, shapeNode) {
	
	this.severity = $shapes.query().match(shapeNode, "sh:severity", "?severity").getNode("?severity");
	if(!this.severity) {
		this.severity = T("sh:Violation");
	}

	this.deactivated = $shapes.query().match(shapeNode, "sh:deactivated", "true").hasSolution();
	this.path = $shapes.query().match(shapeNode, "sh:path", "?path").getNode("?path");
	this.shapeNode = shapeNode;
	this.shapesGraph = shapesGraph;
	this.constraints = [];
	
	var handled = new NodeSet();
	var self = this;
	RDFQuery($shapes).match(shapeNode, "?predicate", "?object").forEach(function(sol) {
		var component = shapesGraph.getComponentWithParameter(sol.predicate);
		if(component && !handled.contains(component.node)) {
			var params = component.getParameters();
			if(params.length == 1) {
				self.constraints.push(new Constraint(self, component, sol.object));
			}
			else if(component.isComplete(shapeNode)) {
				self.constraints.push(new Constraint(self, component));
				handled.add(component.node);
			}
		}
	});
}

Shape.prototype.getConstraints = function() {
	return this.constraints;
}

Shape.prototype.getTargetNodes = function() {
	var results = new NodeSet();
	var dataUtil = new RDFQueryUtil($data);
	var shapesUtil = new RDFQueryUtil($shapes);
	
	if(shapesUtil.isInstanceOf(this.shapeNode, T("rdfs:Class"))) {
		results.addAll(dataUtil.getInstancesOf(this.shapeNode).toArray());
	}
	
	$shapes.query().
		match(this.shapeNode, "sh:targetClass", "?targetClass").forEachNode("?targetClass", function(targetClass) {
			results.addAll(dataUtil.getInstancesOf(targetClass).toArray());
		});
	
	results.addAll($shapes.query().
			match(this.shapeNode, "sh:targetNode", "?targetNode").getNodeArray("?targetNode"));
	
	$shapes.query().
			match(this.shapeNode, "sh:targetSubjectsOf", "?subjectsOf").
			forEachNode("?subjectsOf", function(predicate) {
				results.addAll(RDFQuery($data).match("?subject", predicate, null).getNodeArray("?subject"));
			});
	
	$shapes.query().
			match(this.shapeNode, "sh:targetObjectsOf", "?objectsOf").
			forEachNode("?objectsOf", function(predicate) {
				results.addAll(RDFQuery($data).match(null, predicate, "?object").getNodeArray("?object"));
			});
	
	return results.toArray();
}


Shape.prototype.getValueNodes = function(focusNode) {
	if(this.path) {
		return $data.query().path(focusNode, toRDFQueryPath(this.path), "?object").getNodeArray("?object");
	}
	else {
		return [ focusNode ];
	}
}

Shape.prototype.isPropertyShape = function() {
	return this.path != null;
}


// class ShapesGraph

function ShapesGraph() {
	
	// Collect all defined constraint components
	var components = [];
	new RDFQueryUtil($shapes).getInstancesOf(T("sh:ConstraintComponent")).forEach(function(node) {
		if(!T("dash:ParameterConstraintComponent").equals(node)) {
			components.push(new ConstraintComponent(node));
		}
	});
	this.components = components;
	
	// Build map from parameters to constraint components
	this.parametersMap = {};
	for(var i = 0; i < this.components.length; i++) {
		var component = this.components[i];
		var parameters = component.getParameters();
		for(var j = 0; j < parameters.length; j++) {
			this.parametersMap[parameters[j].value] = component;
		}
	}

	// Collection of shapes is populated on demand - here we remember the instances
	this.shapes = {}; // Keys are the URIs/bnode ids of the shape nodes
}


ShapesGraph.prototype.getComponentWithParameter = function(parameter) {
	return this.parametersMap[parameter.value];
}

ShapesGraph.prototype.getShape = function(shapeNode) {
	var shape = this.shapes[shapeNode.value];
	if(!shape) {
		shape = new Shape(this, shapeNode);
		this.shapes[shapeNode.value] = shape;
	}
	return shape;
}

ShapesGraph.prototype.getShapeNodesWithConstraints = function() {
	if(!this.shapeNodesWithConstraints) {
		var set = new NodeSet();
		for(var i = 0; i < this.components.length; i++) {
			var params = this.components[i].requiredParameters;
			for(var j = 0; j < params.length; j++) {
				RDFQuery($shapes).match("?shape", params[j], null).addAllNodes("?shape", set);
			}
		}
		this.shapeNodesWithConstraints = set.toArray();
	}
	return this.shapeNodesWithConstraints;
}

ShapesGraph.prototype.getShapesWithTarget = function() {
	
	if(!this.targetShapes) {
		this.targetShapes = [];
		var cs = this.getShapeNodesWithConstraints();
		var util = new RDFQueryUtil($shapes);
		for(var i = 0; i < cs.length; i++) {
			var shapeNode = cs[i];
			if(util.isInstanceOf(shapeNode, T("rdfs:Class")) ||
			   RDFQuery($shapes).match(shapeNode, "sh:targetClass", null).hasSolution() ||
			   RDFQuery($shapes).match(shapeNode, "sh:targetNode", null).hasSolution() ||
			   RDFQuery($shapes).match(shapeNode, "sh:targetSubjectsOf", null).hasSolution() ||
			   RDFQuery($shapes).match(shapeNode, "sh:targetObjectsOf", null).hasSolution() ||
			   RDFQuery($shapes).match(shapeNode, "sh:target", null).hasSolution()) {
				this.targetShapes.push(this.getShape(shapeNode));
			}
		}
	}
	
	return this.targetShapes;
}


// class ValidationFunction

var globalObject = this;

function ValidationFunction(functionName, parameters) {
	
	this.funcName = functionName;
	this.func = globalObject[functionName];
	if(!this.func) {
		throw "Cannot find validator function " + functionName;
	}
	// Get list of argument of the function, see
	// https://davidwalsh.name/javascript-arguments
	var args = this.func.toString().match(/function\s.*?\(([^)]*)\)/)[1];
	var funcArgsRaw = args.split(',').map(function(arg) {
			return arg.replace(/\/\*.*\*\//, '').trim();
	  	}).filter(function(arg) {
	  		return arg;
	  	});
	this.funcArgs = []; 
	this.parameters = [];
	for(var i = 0; i < funcArgsRaw.length; i++) {
		var arg = funcArgsRaw[i];
		if(arg.indexOf("$") == 0) {
			arg = arg.substring(1);
		}
		this.funcArgs.push(arg);
		for(var j = 0; j < parameters.length; j++) {
			var parameter = parameters[j];
			var localName = getLocalName(parameter.value);
			if(arg === localName) {
				this.parameters[i] = parameter;
				break;
			}
		}
	}
}

ValidationFunction.prototype.doExecute = function(args) {
	return this.func.apply(globalObject, args);
}

ValidationFunction.prototype.execute = function(focusNode, valueNode, constraint) {
	var args = [];
	for(var i = 0; i < this.funcArgs.length; i++) {
		var arg = this.funcArgs[i];
		var param = this.parameters[i];
		if(param) {
			var value = constraint.getParameterValue(arg);
			args.push(value);
		}
		else if(arg === "focusNode") {
			args.push(focusNode);
		}
		else if(arg === "value") {
			args.push(valueNode);
		}
		else if(arg === "currentShape") {
			args.push(constraint.shape.shapeNode);
		}
		else if(arg === "path") {
			args.push(constraint.shape.path);
		}
		else if(arg == "shapesGraph") {
			args.push("DummyShapesGraph");
		}
		else {
			throw "Unexpected validator function argument " + arg + " for function " + this.funcName;
		}
	}
	return this.doExecute(args);
}


// class ValidationEngine

var currentShapesGraph; // Singleton needed for access by the SHACL object

function ValidationEngine(shapesGraph, conformanceOnly) {
	this.conformanceOnly = conformanceOnly;
	this.conforms = true;
	this.results = [];
	this.shapesGraph = shapesGraph;
}

ValidationEngine.prototype.addResultProperty = function(result, predicate, object) {
	this.results.push([result, predicate, object]);
}

ValidationEngine.prototype.createResult = function(constraint, focusNode, valueNode) {
	var result = this.createResultObject();
	this.conforms = false;
	this.addResultProperty(result, T("rdf:type"), T("sh:ValidationResult"));
	this.addResultProperty(result, T("sh:resultSeverity"), constraint.shape.severity);
	this.addResultProperty(result, T("sh:sourceConstraintComponent"), constraint.component.node);
	this.addResultProperty(result, T("sh:sourceShape"), constraint.shape.shapeNode);
	this.addResultProperty(result, T("sh:focusNode"), focusNode);
	if(valueNode) {
		this.addResultProperty(result, T("sh:value"), valueNode);
	}
	return result;
}

ValidationEngine.prototype.createResultObject = function() {
	return TermFactory.blankNode();
}

ValidationEngine.prototype.createResultFromObject = function(obj, constraint, focusNode, valueNode) {
	if(obj === false) {
		if(this.conformanceOnly) {
			return true;
		}
		var result = this.createResult(constraint, focusNode, valueNode);
		if(constraint.shape.isPropertyShape()) {
			this.addResultProperty(result, T("sh:resultPath"), constraint.shape.path); // TODO: Make deep copy
		}
		this.createResultMessages(result, constraint);
	}
	else if(typeof obj === 'string') {
		if(this.conformanceOnly) {
			return true;
		}
		var result = this.createResult(constraint, focusNode, valueNode);
		if(constraint.shape.isPropertyShape()) {
			this.addResultProperty(result, T("sh:resultPath"), constraint.shape.path); // TODO: Make deep copy
		}
		this.addResultProperty(result, T("sh:resultMessage"), TermFactory.literal(obj, T("xsd:string")));
		this.createResultMessages(result, constraint);
	}
	else if(typeof obj === 'object') {
		if(this.conformanceOnly) {
			return true;
		}
		var result = this.createResult(constraint, focusNode);
		if(obj.path) {
			this.addResultProperty(result, T("sh:resultPath"), obj.path); // TODO: Make deep copy
		}
		else if(constraint.shape.isPropertyShape()) {
			this.addResultProperty(result, T("sh:resultPath"), constraint.shape.path); // TODO: Make deep copy
		}
		if(obj.value) {
			this.addResultProperty(result, T("sh:value"), obj.value);
		}
		else if(valueNode) {
			this.addResultProperty(result, T("sh:value"), valueNode);
		}
		if(obj.message) {
			this.addResultProperty(result, T("sh:resultMessage"), TermFactory.literal(obj.message, T("xsd:string")));
		}
		else {
			this.createResultMessages(result, constraint);
		}
	}
	return false;
}

ValidationEngine.prototype.createResultMessages = function(result, constraint) {
	var ms = $shapes.query().
			match(constraint.shape.shapeNode, "sh:message", "?message").
			getNodeArray("?message");
	if(ms.length == 0) {
		var generic = constraint.shape.isPropertyShape() ? 
				constraint.component.propertyValidationFunctionGeneric :
				constraint.component.nodeValidationFunctionGeneric;
		var predicate = generic ? T("sh:validator") : (constraint.shape.isPropertyShape() ? T("sh:propertyValidator") : T("sh:nodeValidator"));
		ms = $shapes.query().
			match(constraint.component.node, predicate, "?validator").
			match("?validator", "sh:message", "?message").
			getNodeArray("?message");
	}
	if(ms.length == 0) {
		ms = $shapes.query().
			match(constraint.component.node, "sh:message", "?message").
			getNodeArray("?message");
	}
	for(var i = 0; i < ms.length; i++) {
		var m = ms[i];
		var str = this.withSubstitutions(m, constraint);
		this.addResultProperty(result, T("sh:resultMessage"), TermFactory.literal(str, m.language || T("xsd:string")));
	}
}

ValidationEngine.prototype.validateAll = function() {
	var shapes = this.shapesGraph.getShapesWithTarget();
	for(var i = 0; i < shapes.length; i++) {
		var shape = shapes[i];
		var focusNodes = shape.getTargetNodes();
		for(var j = 0; j < focusNodes.length; j++) {
			if(this.validateNodeAgainstShape(focusNodes[j], shape)) {
				return true;
			}
		}
	}
	return false;
}

// Returns true if any violation has been found
ValidationEngine.prototype.validateNodeAgainstShape = function(focusNode, shape) {
	if(shape.deactivated) {
		return false;
	}
	var constraints = shape.getConstraints();
	var valueNodes = shape.getValueNodes(focusNode);
	for(var i = 0; i < constraints.length; i++) {
		if(this.validateNodeAgainstConstraint(focusNode, valueNodes, constraints[i])) {
			return true;
		}
	}
	return false;
}

ValidationEngine.prototype.validateNodeAgainstConstraint = function(focusNode, valueNodes, constraint) {
	currentShapesGraph = this.shapesGraph;
	if(T("sh:PropertyConstraintComponent").equals(constraint.component.node)) {
		for(var i = 0; i < valueNodes.length; i++) {
			if(this.validateNodeAgainstShape(valueNodes[i], this.shapesGraph.getShape(constraint.paramValue))) {
				return true;
			}
		}
	}
	else {
		var validationFunction = constraint.shape.isPropertyShape() ? 
				constraint.component.propertyValidationFunction : 
				constraint.component.nodeValidationFunction;
		if(validationFunction) {
			var generic = constraint.shape.isPropertyShape() ?
					constraint.component.propertyValidationFunctionGeneric :
					constraint.component.nodeValidationFunctionGeneric;
			if(generic) {
				// Generic sh:validator is called for each value node separately
				for(var i = 0; i < valueNodes.length; i++) {
					var valueNode = valueNodes[i];
					var obj = validationFunction.execute(focusNode, valueNode, constraint);
					if(Array.isArray(obj)) {
						for(var a = 0; a < obj.length; a++) {
							if(this.createResultFromObject(obj[a], constraint, focusNode, valueNode)) {
								return true;
							}
						}
					}
					else {
						if(this.createResultFromObject(obj, constraint, focusNode, valueNode)) {
							return true;
						}
					}
				}
			}
			else {
				var obj = validationFunction.execute(focusNode, null, constraint);
				if(Array.isArray(obj)) {
					for(var a = 0; a < obj.length; a++) {
						if(this.createResultFromObject(obj[a], constraint, focusNode, valueNode)) {
							return true;
						}
					}
				}
				else {
					if(this.createResultFromObject(obj, constraint, focusNode)) {
						return true;
					}
				}
			}
		}
		else {
			throw "Cannot find validator for constraint component " + constraint.component.node.value;
		}
	}
	return false;
}

ValidationEngine.prototype.withSubstitutions = function(msg, constraint) {
	var str = msg.lex;
	var values = constraint.parameterValues;
	for(var key in values) {
		var label = nodeLabel(values[key], shapesStore);
		str = str.replace("{$" + key + "}", label);
		str = str.replace("{?" + key + "}", label);
	}
	return TermFactory.literal(str, msg.language | msg.datatype);
}
