var rdfquery = require("./rdfquery");
var T = rdfquery.T;
var TermFactory = require("./rdfquery/term-factory");

var nodeLabel = function (node, store) {
    if (node.termType === "Collection") {
        var acc = [];
        for (var i=0; i<node.elements.length; i++) {
            acc.push(nodeLabel(node.elements[i], store));
        }
        return acc.join(", ");
    }
    if (node.isURI()) {
        for (prefix in store.namespaces) {
            var ns = store.namespaces[prefix];
            if (node.value.indexOf(ns) === 0) {
                return prefix + ":" + node.value.substring(ns.length);
            }
        }
        return "<" + node.value + ">";
    }
    else if (node.isBlankNode()) {
        return "Blank node " + node.toString();
    }
    else {
        return "" + node;
    }
};

// class ValidationEngine

var ValidationEngine = function (context, conformanceOnly) {
    this.context = context;
    this.conformanceOnly = conformanceOnly;
    this.results = [];
};

ValidationEngine.prototype.addResultProperty = function (result, predicate, object) {
    this.results.push([result, predicate, object]);
};

/**
 * Creates a new BlankNode holding the SHACL validation result, adding the default
 * properties for the constraint, focused node and value node
 */
ValidationEngine.prototype.createResult = function (constraint, focusNode, valueNode) {
    var result = TermFactory.blankNode();
    this.addResultProperty(result, T("rdf:type"), T("sh:ValidationResult"));
    this.addResultProperty(result, T("sh:resultSeverity"), constraint.shape.severity);
    this.addResultProperty(result, T("sh:sourceConstraintComponent"), constraint.component.node);
    this.addResultProperty(result, T("sh:sourceShape"), constraint.shape.shapeNode);
    this.addResultProperty(result, T("sh:focusNode"), focusNode);
    if (valueNode) {
        this.addResultProperty(result, T("sh:value"), valueNode);
    }
    return result;
};

/**
 * Creates all the validation result nodes and messages for the result of applying the validation logic
 * of a constraints against a node.
 * Result passed as the first argument can be false, a resultMessage or a validation result object.
 * If none of these values is passed no error result or error message will be created.
 */
ValidationEngine.prototype.createResultFromObject = function (obj, constraint, focusNode, valueNode) {
    if (obj === false) {
        if (this.conformanceOnly) {
            return true;
        }
        var result = this.createResult(constraint, focusNode, valueNode);
        if (constraint.shape.isPropertyShape()) {
            this.addResultProperty(result, T("sh:resultPath"), constraint.shape.path); // TODO: Make deep copy
        }
        this.createResultMessages(result, constraint);
    }
    else if (typeof obj === 'string') {
        if (this.conformanceOnly) {
            return true;
        }
        result = this.createResult(constraint, focusNode, valueNode);
        if (constraint.shape.isPropertyShape()) {
            this.addResultProperty(result, T("sh:resultPath"), constraint.shape.path); // TODO: Make deep copy
        }
        this.addResultProperty(result, T("sh:resultMessage"), TermFactory.literal(obj, T("xsd:string")));
        this.createResultMessages(result, constraint);
    }
    else if (typeof obj === 'object') {
        if (this.conformanceOnly) {
            return true;
        }
        result = this.createResult(constraint, focusNode);
        if (obj.path) {
            this.addResultProperty(result, T("sh:resultPath"), obj.path); // TODO: Make deep copy
        }
        else if (constraint.shape.isPropertyShape()) {
            this.addResultProperty(result, T("sh:resultPath"), constraint.shape.path); // TODO: Make deep copy
        }
        if (obj.value) {
            this.addResultProperty(result, T("sh:value"), obj.value);
        }
        else if (valueNode) {
            this.addResultProperty(result, T("sh:value"), valueNode);
        }
        if (obj.message) {
            this.addResultProperty(result, T("sh:resultMessage"), TermFactory.literal(obj.message, T("xsd:string")));
        }
        else {
            this.createResultMessages(result, constraint);
        }
    }
    return false;
};

/**
 * Creates a result message from the result and the message pattern in the constraint
 */
ValidationEngine.prototype.createResultMessages = function (result, constraint) {
    var ms = this.context.$shapes.query()
        .match(constraint.shape.shapeNode, "sh:message", "?message")
        .getNodeArray("?message");
    if (ms.length === 0) {
        var generic = constraint.shape.isPropertyShape() ?
            constraint.component.propertyValidationFunctionGeneric :
            constraint.component.nodeValidationFunctionGeneric;
        var predicate = generic ? T("sh:validator") : (constraint.shape.isPropertyShape() ? T("sh:propertyValidator") : T("sh:nodeValidator"));
        ms = this.context.$shapes.query()
            .match(constraint.component.node, predicate, "?validator")
            .match("?validator", "sh:message", "?message")
            .getNodeArray("?message");
    }
    if (ms.length === 0) {
        ms = this.context.$shapes.query()
            .match(constraint.component.node, "sh:message", "?message")
            .getNodeArray("?message");
    }
    for (var i = 0; i < ms.length; i++) {
        var m = ms[i];
        var str = this.withSubstitutions(m, constraint);
        this.addResultProperty(result, T("sh:resultMessage"), str);
    }
};

/**
 * Validates the data graph against the shapes graph
 */
ValidationEngine.prototype.validateAll = function (rdfDataGraph) {
    var shapes = this.context.shapesGraph.getShapesWithTarget();
    for (var i = 0; i < shapes.length; i++) {
        var shape = shapes[i];
        var focusNodes = shape.getTargetNodes(rdfDataGraph);
        for (var j = 0; j < focusNodes.length; j++) {
            if (this.validateNodeAgainstShape(focusNodes[j], shape, rdfDataGraph)) {
                return true;
            }
        }
    }
    return false;
};

/**
 * Returns true if any violation has been found
 */
ValidationEngine.prototype.validateNodeAgainstShape = function (focusNode, shape, rdfDataGraph) {
    if (shape.deactivated) {
        return false;
    }
    var constraints = shape.getConstraints();
    var valueNodes = shape.getValueNodes(focusNode, rdfDataGraph);
    for (var i = 0; i < constraints.length; i++) {
        if (this.validateNodeAgainstConstraint(focusNode, valueNodes, constraints[i], rdfDataGraph)) {
            return true;
        }
    }
    return false;
};

ValidationEngine.prototype.validateNodeAgainstConstraint = function (focusNode, valueNodes, constraint, rdfDataGraph) {
    if (T("sh:PropertyConstraintComponent").equals(constraint.component.node)) {
        for (var i = 0; i < valueNodes.length; i++) {
            if (this.validateNodeAgainstShape(valueNodes[i], this.context.shapesGraph.getShape(constraint.paramValue), rdfDataGraph)) {
                return true;
            }
        }
    }
    else {
        var validationFunction = constraint.shape.isPropertyShape() ?
            constraint.component.propertyValidationFunction :
            constraint.component.nodeValidationFunction;
        if (validationFunction) {
            var generic = constraint.shape.isPropertyShape() ?
                constraint.component.propertyValidationFunctionGeneric :
                constraint.component.nodeValidationFunctionGeneric;
            if (generic) {
                // Generic sh:validator is called for each value node separately
                for (i = 0; i < valueNodes.length; i++) {
                    var valueNode = valueNodes[i];
                    var obj = validationFunction.execute(focusNode, valueNode, constraint);
                    if (Array.isArray(obj)) {
                        for (a = 0; a < obj.length; a++) {
                            if (this.createResultFromObject(obj[a], constraint, focusNode, valueNode)) {
                                return true;
                            }
                        }
                    }
                    else {
                        if (this.createResultFromObject(obj, constraint, focusNode, valueNode)) {
                            return true;
                        }
                    }
                }
            }
            else {
                obj = validationFunction.execute(focusNode, null, constraint);
                if (Array.isArray(obj)) {
                    for (var a = 0; a < obj.length; a++) {
                        if (this.createResultFromObject(obj[a], constraint, focusNode)) {
                            return true;
                        }
                    }
                }
                else {
                    if (this.createResultFromObject(obj, constraint, focusNode)) {
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
};

ValidationEngine.prototype.withSubstitutions = function (msg, constraint) {
    var str = msg.lex;
    var values = constraint.parameterValues;
    for (var key in values) {
        var label = nodeLabel(values[key], this.context.$shapes);
        str = str.replace("{$" + key + "}", label);
        str = str.replace("{?" + key + "}", label);
    }
    return TermFactory.literal(str, msg.language | msg.datatype);
};

module.exports = ValidationEngine;