/**
 * Created by antoniogarrote on 08/05/2017.
 */
$rdf = require("rdflib");
require("./rdfquery");

TermFactory.impl = $rdf;

var exLiteral = $rdf.literal("a", "de");
Object.defineProperty(Object.getPrototypeOf(exLiteral), "lex", { get: function () { return this.value } });
Object.getPrototypeOf(exLiteral).isBlankNode = function () { return false };
Object.getPrototypeOf(exLiteral).isLiteral = function () { return true };
Object.getPrototypeOf(exLiteral).isURI = function () { return false };

var exBlankNode = $rdf.blankNode();
Object.getPrototypeOf(exBlankNode).isBlankNode = function () { return true };
Object.getPrototypeOf(exBlankNode).isLiteral = function () { return false };
Object.getPrototypeOf(exBlankNode).isURI = function () { return false };

var exNamedNode = $rdf.namedNode("urn:x-dummy");
Object.getPrototypeOf(exNamedNode).isBlankNode = function () { return false };
Object.getPrototypeOf(exNamedNode).isLiteral = function () { return false };
Object.getPrototypeOf(exNamedNode).isURI = function () { return true };

RDFLibGraph = function (store) {
    this.store = store;
}

RDFLibGraph.prototype.find = function (s, p, o) {
    return new RDFLibGraphIterator(this.store, s, p, o);
}

RDFLibGraph.prototype.query = function () {
    return RDFQuery(this);
}

RDFLibGraphIterator = function (store, s, p, o) {
    this.index = 0;
    this.ss = store.statementsMatching(s, p, o);
}

RDFLibGraphIterator.prototype.close = function () {
    // Do nothing
}

RDFLibGraphIterator.prototype.next = function () {
    if (this.index >= this.ss.length) {
        return null;
    }
    else {
        return this.ss[this.index++];
    }
}

require("./dash");
require("./shacl-validator");

var fs = require("fs");

console.log("Loaded!");


/********************************/
/* Vocabularies                 */
/********************************/
shapesGraphURI = "urn:x-shacl:shapesGraph";
dataGraphURI = "urn:x-shacl:dataGraph";
shaclFile = fs.readFileSync("./shacl.ttl").toString();
dashFile = fs.readFileSync("./dash.ttl").toString();
/********************************/
/********************************/

results = null;
shapesStore = $rdf.graph();
dataStore = $rdf.graph();
shapesGraph = null;
validationEngine = null;
validationError = null;
sequence = null;

$shapes = new RDFLibGraph(shapesStore);
shapesGraph = new ShapesGraph();


function postProcessGraph(store, graphURI, newStore) {

    var ss = newStore.statementsMatching(undefined, undefined, undefined);
    for (var i = 0; i < ss.length; i++) {
        var object = ss[i].object;
        if (T("xsd:boolean").equals(object.datatype)) {
            if ("0" == object.value || "false" === object.value) {
                store.add(ss[i].subject, ss[i].predicate, T("false"), graphURI);
            }
            else {
                store.add(ss[i].subject, ss[i].predicate, T("true"), graphURI);
            }
        }
        else if (object.termType === 'collection') {
            var items = object.elements;
            store.add(ss[i].subject, ss[i].predicate, createRDFListNode(store, items, 0));
        }
        else {
            store.add(ss[i].subject, ss[i].predicate, ss[i].object, graphURI);
        }
    }

    for (var prefix in newStore.namespaces) {
        var ns = newStore.namespaces[prefix];
        store.namespaces[prefix] = ns;
    }
}

var defaultHandleError = function (ex) {
    console.log("ERROR " + ex);
    console.log(ex);
}

function loadGraph(str, store, graphURI, mimeType, andThen, handleError) {
    var newStore = $rdf.graph();
    handleError = handleError || defaultHandleError;
    if (mimeType === "application/ld+json") {
        var error = false;
        $rdf.parse(str, newStore, graphURI, mimeType, function (err, kb) {
            if (err) {
                error = true;
                handleError(err)
            }
            else if (!error) {
                postProcessGraph(store, graphURI, newStore);
                andThen();
            }
        });
    }
    else {
        try {
            $rdf.parse(str, newStore, graphURI, mimeType);
            postProcessGraph(store, graphURI, newStore);
            andThen();
        }
        catch (ex) {
            handleError(ex);
        }
    }
}

function parseDataGraph(text, mediaType, andThen) {
    var newStore = $rdf.graph();
    loadGraph(text, newStore, dataGraphURI, mediaType, function () {
        dataStore = newStore;
        $data = new RDFLibGraph(dataStore);
        andThen();
    }, function (ex) {
        showError(ex);
    });
}

function updateValidationEngine() {
    results = [];
    validationEngine = new ValidationEngine(shapesGraph);
    try {
        validationError = null;
        if (sequence) {
            sequence = [];
        }
        validationEngine.validateAll();
    }
    catch (ex) {
        validationError = ex;
    }
}

function showStatus(s) { console.log(s); }
function showError(s) { console.log(s); }

nodeLabel = function(node, store) {
    if (node.isURI()) {
        for (prefix in store.namespaces) {
            var ns = store.namespaces[prefix];
            if (node.value.indexOf(ns) == 0) {
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
}

function showValidationResults() {
    if (validationError) {
        console.log("(Failure)");
        console.log("VALIDATION FAILURE: " + validationError);
    }
    else {
        console.log("(Valid)");
        console.log("Found " + results.length + " results");
        var str = "";
        for (var i = 0; i < results.length; i++) {
            str += results[i].str;
            str += "\n] .\n";
        }
        console.log(str);

        if (sequence) {
            for (var i = 0; i < sequence.length; i++) {
                var s = sequence[i];
                var text = "";
                text += s.f.funcName + "(";
                for (var a = 0; a < s.args.length; a++) {
                    if (a > 0) {
                        text += ", ";
                    }
                    var arg = s.args[a];
                    if (!arg) {
                        text += "null";
                    }
                    else {
                        text += nodeLabel(arg, dataStore);
                    }
                }
                text += ")";
                console.log(text);
                if (s.count) {
                    var span = " -> " + s.count + " violations";
                    console.log(span);
                }
            }
        }
    }
}

function parseShapesGraph(text, mediaType, andThen) {
    var handleError = function (ex) {
        console.log("ERROR " + ex);
        console.log(ex);
    }
    var newShapesStore = $rdf.graph();
    loadGraph(text, newShapesStore, shapesGraphURI, mediaType, function () {
        loadGraph(shaclFile, newShapesStore, "http://shacl.org", "text/turtle", function () {
            loadGraph(dashFile, newShapesStore, "http://datashapes.org/dash", "text/turtle", function () {
                shapesStore = newShapesStore;
                $shapes = new RDFLibGraph(shapesStore);
                andThen();
            });
        }, handleError);
    }, handleError);
}


function updateDataGraph(text, mediaType, cb) {
    var startTime = new Date().getTime();
    parseDataGraph(text, mediaType, function () {
        var midTime = new Date().getTime();
        updateValidationEngine();
        var endTime = new Date().getTime();
        showStatus("Parsing took " + (midTime - startTime) + " ms. Validating the data took " + (endTime - midTime) + " ms.");
        showValidationResults();
        if (cb) {
            cb();
        }
    });
}


function updateShapesGraph(shapes, mediaType, cb) {
    var startTime = new Date().getTime();
    parseShapesGraph(shapes, mediaType, function () {
        var midTime = new Date().getTime();
        shapesGraph = new ShapesGraph();
        var midTime2 = new Date().getTime();
        updateValidationEngine();
        var endTime = new Date().getTime();
        showStatus("Parsing took " + (midTime - startTime) + " ms. Preparing the shapes took " + (midTime2 - midTime) + " ms. Validation the data took " + (endTime - midTime2) + " ms.");
        showValidationResults();
        if (cb) {
            cb();
        }
    });
}
function createRDFListNode(store, items, index) {
    if (index >= items.length) {
        return T("rdf:nil");
    }
    else {
        var bnode = TermFactory.blankNode();
        store.add(bnode, T("rdf:first"), items[index]);
        store.add(bnode, T("rdf:rest"), createRDFListNode(store, items, index + 1));
        return bnode;
    }
}

ValidationEngine.prototype.addResultProperty = function(result, predicate, object) {
    result.str += "\n\t";
    if(T("rdf:type").equals(predicate)) {
        result.str += "a ";
    }
    else {
        result.str += nodeLabel(predicate, shapesStore) + " ";
    }
    if(object.isURI()) {
        result.str += nodeLabel(object, shapesStore);
    }
    else if(object.isBlankNode()) {
        result.str += object.toString();
    }
    else {
        if(T("xsd:boolean").equals(object.datatype) || T("xsd:integer").equals(object.datatype)) {
            result.str += object.lex;
        }
        else {
            result.str += '"' + object.lex + '"';
            if(object.language) {
                result.str += '@' + object.language;
            }
            else if(!T("xsd:string").equals(object.datatype)) {
                result.str += '^^' + nodeLabel(object.datatype, shapesStore);
            }
        }
    }
    result.str += " ;";
}

ValidationEngine.prototype.createResultObject = function() {
    var result = { str : "[" };
    results.push(result);
    return result;
}

ValidationFunction.prototype.doExecute = function(args) {
    if(sequence) {
        var s = {f : this, args : args, depth : SHACL.depth };
        sequence.push(s);
        var result = this.func.apply(global, args);
        if(result === false || typeof result === 'string' || typeof result === 'object') {
            s.count = 1;
        }
        else if(Array.isArray(result)) {
            s.count = result.length;
        }
        return result;
    }
    else {
        return this.func.apply(global, args);
    }
}

module.exports.validate = function(data, dataMediaType, shapes, shapesMediaType, cb) {
    updateDataGraph(data, dataMediaType, function () {
        updateShapesGraph(shapes, shapesMediaType, function () {
            console.log("DONE!");
            cb();
        });
    });
};