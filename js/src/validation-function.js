// class ValidationFunction

var rdfquery = require("./rdfquery");
var debug = require("debug")("validation-function");


var globalObject = typeof(window) != null ? window : global;


var ValidationFunction = function (functionName, parameters, functionRegistry) {

    this.funcName = functionName;
    this.func = functionRegistry[functionName];
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
};

ValidationFunction.prototype.doExecute = function (args) {
    return this.func.apply(globalObject, args);
};

ValidationFunction.prototype.execute = function (focusNode, valueNode, constraint) {
    debug("Validating " + this.funcName);
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
};

module.exports = ValidationFunction;