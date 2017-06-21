/* Copyright (C) TopQuadrant, Inc - licensed under AGPL
 * Written by Holger Knublauch <holger@topquadrant.com>, 2017
 */

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

var rdfquery = require("./rdfquery");
var T = rdfquery.T;


rdfquery.TermFactory.registerNamespace("dash", "http://datashapes.org/dash#");


var SHACL = {

    depth: 0,

    compareNodes: function (node1, node2) {
        // TODO: Does not handle the case where nodes cannot be compared
        return compareTerms(node1, node2);
    },

    nodeConformsToShape: function (focusNode, shapeNode) {
        var localEngine = new ValidationEngine(currentShapesGraph, true);
        var shape = currentShapesGraph.getShape(shapeNode);
        try {
            SHACL.depth++;
            console.log("Validating against shape");
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
    for (var i = 0; i < params.length; i++) {
        var param = params[i];
        var value = paramValue ? paramValue : rdfquery.RDFQuery($shapes).match(shape.shapeNode, param, "?value").getNode("?value");
        if (value) {
            var localName = rdfquery.getLocalName(param.uri);
            parameterValues[localName] = value;
        }
    }
    this.parameterValues = parameterValues;
}

Constraint.prototype.getParameterValue = function (paramName) {
    return this.parameterValues[paramName];
}

// class ValidationFunction

var globalObject = global;

var ValidationFunction = function (functionName, parameters) {

    this.funcName = functionName;
    this.func = ValidationFunction.functionRegistry[functionName];
    if (!this.func) {
        throw "Cannot find validator function " + functionName;
    }
    // Get list of argument of the function, see
    // https://davidwalsh.name/javascript-arguments
    var args = this.func.toString().match(/function\s.*?\(([^)]*)\)/)[1];
    var funcArgsRaw = args.split(',').map(function (arg) {
        return arg.replace(/\/\*.*\*\//, '').trim();
    }).filter(function (arg) {
        return arg;
    });
    this.funcArgs = [];
    this.parameters = [];
    for (var i = 0; i < funcArgsRaw.length; i++) {
        var arg = funcArgsRaw[i];
        if (arg.indexOf("$") == 0) {
            arg = arg.substring(1);
        }
        this.funcArgs.push(arg);
        for (var j = 0; j < parameters.length; j++) {
            var parameter = parameters[j];
            var localName = rdfquery.getLocalName(parameter.value);
            if (arg === localName) {
                this.parameters[i] = parameter;
                break;
            }
        }
    }
}

ValidationFunction.functionRegistry = {};

ValidationFunction.prototype.doExecute = function (args) {
    return this.func.apply(globalObject, args);
}

ValidationFunction.prototype.execute = function (focusNode, valueNode, constraint) {
    console.log("VALIDATING " + this.funcName);
    var args = [];
    for (var i = 0; i < this.funcArgs.length; i++) {
        var arg = this.funcArgs[i];
        var param = this.parameters[i];
        if (param) {
            var value = constraint.getParameterValue(arg);
            args.push(value);
        }
        else if (arg === "focusNode") {
            args.push(focusNode);
        }
        else if (arg === "value") {
            args.push(valueNode);
        }
        else if (arg === "currentShape") {
            args.push(constraint.shape.shapeNode);
        }
        else if (arg === "path") {
            args.push(constraint.shape.path);
        }
        else if (arg == "shapesGraph") {
            args.push("DummyShapesGraph");
        }
        else if (arg === "this") {
            args.push(focusNode);
        }
        else {
            throw "Unexpected validator function argument " + arg + " for function " + this.funcName;
        }
    }
    return this.doExecute(args);
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
        match("?parameter", "sh:path", "?path").forEach(function (sol) {
            parameters.push(sol.path);
            parameterNodes.push(sol.parameter);
            if ($shapes.query().match(sol.parameter, "sh:optional", "true").hasSolution()) {
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
    if (!this.nodeValidationFunction) {
        this.nodeValidationFunction = this.findValidationFunction(T("sh:validator"));
        this.nodeValidationFunctionGeneric = true;
    }
    this.propertyValidationFunction = this.findValidationFunction(T("sh:propertyValidator"));
    if (!this.propertyValidationFunction) {
        this.propertyValidationFunction = this.findValidationFunction(T("sh:validator"));
        this.propertyValidationFunctionGeneric = true;
    }
}

ConstraintComponent.prototype.findValidationFunction = function (predicate) {
    var functionName = $shapes.query().
        match(this.node, predicate, "?validator").
        match("?validator", "rdf:type", "sh:JSValidator").
        match("?validator", "sh:jsFunctionName", "?functionName").
        getNode("?functionName");
    if (functionName) {
        return new ValidationFunction(functionName.lex, this.parameters);
    }
    else {
        return null;
    }
}

ConstraintComponent.prototype.getParameters = function () {
    return this.parameters;
}

ConstraintComponent.prototype.isComplete = function (shapeNode) {
    for (var i = 0; i < this.parameters.length; i++) {
        var parameter = this.parameters[i];
        if (!this.isOptional(parameter.uri)) {
            if (!rdfquery.RDFQuery($shapes).match(shapeNode, parameter, null).hasSolution()) {
                return false;
            }
        }
    }
    return true;
}

ConstraintComponent.prototype.isOptional = function (parameterURI) {
    return this.optionals[parameterURI];
}


// class Shape

function Shape(shapesGraph, shapeNode) {

    this.severity = $shapes.query().match(shapeNode, "sh:severity", "?severity").getNode("?severity");
    if (!this.severity) {
        this.severity = T("sh:Violation");
    }

    this.deactivated = $shapes.query().match(shapeNode, "sh:deactivated", "true").hasSolution();
    this.path = $shapes.query().match(shapeNode, "sh:path", "?path").getNode("?path");
    this.shapeNode = shapeNode;
    this.shapesGraph = shapesGraph;
    this.constraints = [];

    var handled = new rdfquery.NodeSet();
    var self = this;
    rdfquery.RDFQuery($shapes).match(shapeNode, "?predicate", "?object").forEach(function (sol) {
        var component = shapesGraph.getComponentWithParameter(sol.predicate);
        if (component && !handled.contains(component.node)) {
            var params = component.getParameters();
            if (params.length == 1) {
                self.constraints.push(new Constraint(self, component, sol.object));
            }
            else if (component.isComplete(shapeNode)) {
                self.constraints.push(new Constraint(self, component));
                handled.add(component.node);
            }
        }
    });
}

Shape.prototype.getConstraints = function () {
    return this.constraints;
};

Shape.prototype.getTargetNodes = function () {
    var results = new rdfquery.NodeSet();
    var dataUtil = new rdfquery.RDFQueryUtil($data);
    var shapesUtil = new rdfquery.RDFQueryUtil($shapes);

    if (shapesUtil.isInstanceOf(this.shapeNode, T("rdfs:Class"))) {
        results.addAll(dataUtil.getInstancesOf(this.shapeNode).toArray());
    }

    $shapes.query().
        match(this.shapeNode, "sh:targetClass", "?targetClass").forEachNode("?targetClass", function (targetClass) {
            results.addAll(dataUtil.getInstancesOf(targetClass).toArray());
        });

    results.addAll($shapes.query().
        match(this.shapeNode, "sh:targetNode", "?targetNode").getNodeArray("?targetNode"));

    $shapes.query().
        match(this.shapeNode, "sh:targetSubjectsOf", "?subjectsOf").
        forEachNode("?subjectsOf", function (predicate) {
            results.addAll(rdfquery.RDFQuery($data).match("?subject", predicate, null).getNodeArray("?subject"));
        });

    $shapes.query().
        match(this.shapeNode, "sh:targetObjectsOf", "?objectsOf").
        forEachNode("?objectsOf", function (predicate) {
            results.addAll(rdfquery.RDFQuery($data).match(null, predicate, "?object").getNodeArray("?object"));
        });

    return results.toArray();
};


Shape.prototype.getValueNodes = function (focusNode) {
    if (this.path) {
        return $data.query().path(focusNode, rdfquery.toRDFQueryPath(this.path), "?object").getNodeArray("?object");
    }
    else {
        return [focusNode];
    }
};

Shape.prototype.isPropertyShape = function () {
    return this.path != null;
};


// class ShapesGraph

var ShapesGraph = function () {

    // Collect all defined constraint components
    var components = [];
    new rdfquery.RDFQueryUtil($shapes).getInstancesOf(T("sh:ConstraintComponent")).forEach(function (node) {
        if (!T("dash:ParameterConstraintComponent").equals(node)) {
            components.push(new ConstraintComponent(node));
        }
    });
    this.components = components;

    // Build map from parameters to constraint components
    this.parametersMap = {};
    for (var i = 0; i < this.components.length; i++) {
        var component = this.components[i];
        var parameters = component.getParameters();
        for (var j = 0; j < parameters.length; j++) {
            this.parametersMap[parameters[j].value] = component;
        }
    }

    // Collection of shapes is populated on demand - here we remember the instances
    this.shapes = {}; // Keys are the URIs/bnode ids of the shape nodes
};


ShapesGraph.prototype.getComponentWithParameter = function (parameter) {
    return this.parametersMap[parameter.value];
};

ShapesGraph.prototype.getShape = function (shapeNode) {
    var shape = this.shapes[shapeNode.value];
    if (!shape) {
        shape = new Shape(this, shapeNode);
        this.shapes[shapeNode.value] = shape;
    }
    return shape;
};

ShapesGraph.prototype.getShapeNodesWithConstraints = function () {
    if (!this.shapeNodesWithConstraints) {
        var set = new rdfquery.NodeSet();
        for (var i = 0; i < this.components.length; i++) {
            var params = this.components[i].requiredParameters;
            for (var j = 0; j < params.length; j++) {
                rdfquery.RDFQuery($shapes).match("?shape", params[j], null).addAllNodes("?shape", set);
            }
        }
        this.shapeNodesWithConstraints = set.toArray();
    }
    return this.shapeNodesWithConstraints;
};

ShapesGraph.prototype.getShapesWithTarget = function () {

    if (!this.targetShapes) {
        this.targetShapes = [];
        var cs = this.getShapeNodesWithConstraints();
        var util = new rdfquery.RDFQueryUtil($shapes);
        for (var i = 0; i < cs.length; i++) {
            var shapeNode = cs[i];
            if (util.isInstanceOf(shapeNode, T("rdfs:Class")) ||
                rdfquery.RDFQuery($shapes).match(shapeNode, "sh:targetClass", null).hasSolution() ||
                rdfquery.RDFQuery($shapes).match(shapeNode, "sh:targetNode", null).hasSolution() ||
                rdfquery.RDFQuery($shapes).match(shapeNode, "sh:targetSubjectsOf", null).hasSolution() ||
                rdfquery.RDFQuery($shapes).match(shapeNode, "sh:targetObjectsOf", null).hasSolution() ||
                rdfquery.RDFQuery($shapes).match(shapeNode, "sh:target", null).hasSolution()) {
                this.targetShapes.push(this.getShape(shapeNode));
            }
        }
    }

    return this.targetShapes;
};

module.exports.SHACL = SHACL;
module.exports.ShapesGraph = ShapesGraph;
module.exports.ValidationFunction = ValidationFunction;