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
// a given ShapesGraph and operates on the current $data().
// It basically walks through all Shapes that have target nodes and runs the validators
// for each Constraint of the shape, producing results along the way.

var rdfquery = require("./rdfquery");
var TermFactory = require("./rdfquery/term-factory");
var NodeSet = require("./rdfquery/node-set");
var T = rdfquery.T;
var ValidationFunction = require("./validation-function");

TermFactory.registerNamespace("dash", "http://datashapes.org/dash#");


// class Constraint

var Constraint = function(shape, component, paramValue, rdfShapesGraph) {
    this.shape = shape;
    this.component = component;
    this.paramValue = paramValue;
    var parameterValues = {};
    var params = component.getParameters();
    for (var i = 0; i < params.length; i++) {
        var param = params[i];
        var value = paramValue ? paramValue : rdfShapesGraph.query().match(shape.shapeNode, param, "?value").getNode("?value");
        if (value) {
            var localName = rdfquery.getLocalName(param.uri);
            parameterValues[localName] = value;
        }
    }
    this.parameterValues = parameterValues;
};

Constraint.prototype.getParameterValue = function (paramName) {
    return this.parameterValues[paramName];
};

// class ConstraintComponent

var ConstraintComponent = function(node, context) {
    this.context = context;
    this.node = node;
    var parameters = [];
    var parameterNodes = [];
    var requiredParameters = [];
    var optionals = {};
    var that = this;
    this.context.$shapes.query().
        match(node, "sh:parameter", "?parameter").
        match("?parameter", "sh:path", "?path").forEach(function (sol) {
            parameters.push(sol.path);
            parameterNodes.push(sol.parameter);
            if (that.context.$shapes.query().match(sol.parameter, "sh:optional", "true").hasSolution()) {
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
};

ConstraintComponent.prototype.findValidationFunction = function (predicate) {
    var functionName = this.context.$shapes.query().
        match(this.node, predicate, "?validator").
        match("?validator", "rdf:type", "sh:JSValidator").
        match("?validator", "sh:jsFunctionName", "?functionName").
        getNode("?functionName");
    if (functionName) {
        return new ValidationFunction(functionName.lex, this.parameters, this.context.functionRegistry);
    }
    else {
        return null;
    }
};

ConstraintComponent.prototype.getParameters = function () {
    return this.parameters;
};

ConstraintComponent.prototype.isComplete = function (shapeNode) {
    for (var i = 0; i < this.parameters.length; i++) {
        var parameter = this.parameters[i];
        if (!this.isOptional(parameter.uri)) {
            if (!this.context.$shapes.query().match(shapeNode, parameter, null).hasSolution()) {
                return false;
            }
        }
    }
    return true;
};

ConstraintComponent.prototype.isOptional = function (parameterURI) {
    return this.optionals[parameterURI];
};


// class Shape

var Shape = function(context, shapeNode) {

    this.context = context;
    this.severity = context.$shapes.query().match(shapeNode, "sh:severity", "?severity").getNode("?severity");
    if (!this.severity) {
        this.severity = T("sh:Violation");
    }

    this.deactivated = context.$shapes.query().match(shapeNode, "sh:deactivated", "true").hasSolution();
    this.path = context.$shapes.query().match(shapeNode, "sh:path", "?path").getNode("?path");
    this.shapeNode = shapeNode;
    this.constraints = [];

    var handled = new NodeSet();
    var self = this;
    var that = this;
    context.$shapes.query().match(shapeNode, "?predicate", "?object").forEach(function (sol) {
        var component = that.context.shapesGraph.getComponentWithParameter(sol.predicate);
        if (component && !handled.contains(component.node)) {
            var params = component.getParameters();
            if (params.length === 1) {
                self.constraints.push(new Constraint(self, component, sol.object, context.$shapes));
            }
            else if (component.isComplete(shapeNode)) {
                self.constraints.push(new Constraint(self, component, undefined, context.$shapes));
                handled.add(component.node);
            }
        }
    });
};

Shape.prototype.getConstraints = function () {
    return this.constraints;
};

Shape.prototype.getTargetNodes = function (rdfDataGraph) {
    var results = new NodeSet();

    if (rdfquery.isInstanceOf(this.shapeNode, T("rdfs:Class"), this.context)) {
        results.addAll(rdfDataGraph.query().getInstancesOf(this.shapeNode).toArray());
    }

    this.context.$shapes.query().
        match(this.shapeNode, "sh:targetClass", "?targetClass").forEachNode("?targetClass", function (targetClass) {
            results.addAll(rdfDataGraph.query().getInstancesOf(targetClass).toArray());
        });

    results.addAll(this.context.$shapes.query().
        match(this.shapeNode, "sh:targetNode", "?targetNode").getNodeArray("?targetNode"));

    this.context.$shapes.query().
        match(this.shapeNode, "sh:targetSubjectsOf", "?subjectsOf").
        forEachNode("?subjectsOf", function (predicate) {
            results.addAll(rdfDataGraph.query().match("?subject", predicate, null).getNodeArray("?subject"));
        });

    this.context.$shapes.query().
        match(this.shapeNode, "sh:targetObjectsOf", "?objectsOf").
        forEachNode("?objectsOf", function (predicate) {
            results.addAll(rdfDataGraph.query().match(null, predicate, "?object").getNodeArray("?object"));
        });

    return results.toArray();
};


Shape.prototype.getValueNodes = function (focusNode, rdfDataGraph) {
    if (this.path) {
        return rdfDataGraph.query().path(focusNode, rdfquery.toRDFQueryPath(this.path, this.context), "?object").getNodeArray("?object");
    }
    else {
        return [focusNode];
    }
};

Shape.prototype.isPropertyShape = function () {
    return this.path != null;
};


// class ShapesGraph

var ShapesGraph = function (context) {

    this.context = context;

    // Collect all defined constraint components
    var components = [];
    this.context.$shapes.query().getInstancesOf(T("sh:ConstraintComponent")).forEach(function (node) {
        if (!T("dash:ParameterConstraintComponent").equals(node)) {
            components.push(new ConstraintComponent(node, context));
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
        shape = new Shape(this.context, shapeNode);
        this.shapes[shapeNode.value] = shape;
    }
    return shape;
};

ShapesGraph.prototype.getShapeNodesWithConstraints = function () {
    if (!this.shapeNodesWithConstraints) {
        var set = new NodeSet();
        for (var i = 0; i < this.components.length; i++) {
            var params = this.components[i].requiredParameters;
            for (var j = 0; j < params.length; j++) {
                this.context.$shapes.query().match("?shape", params[j], null).addAllNodes("?shape", set);
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
        for (var i = 0; i < cs.length; i++) {
            var shapeNode = cs[i];
            if (rdfquery.isInstanceOf(shapeNode, T("rdfs:Class"), this.context) ||
                this.context.$shapes.query().match(shapeNode, "sh:targetClass", null).hasSolution() ||
                this.context.$shapes.query().match(shapeNode, "sh:targetNode", null).hasSolution() ||
                this.context.$shapes.query().match(shapeNode, "sh:targetSubjectsOf", null).hasSolution() ||
                this.context.$shapes.query().match(shapeNode, "sh:targetObjectsOf", null).hasSolution() ||
                this.context.$shapes.query().match(shapeNode, "sh:target", null).hasSolution()) {
                this.targetShapes.push(this.getShape(shapeNode));
            }
        }
    }

    return this.targetShapes;
};

module.exports = ShapesGraph;