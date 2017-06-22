/**
 * Created by antoniogarrote on 08/05/2017.
 */

var jsonld = require("jsonld");
var rdfquery = require("./src/rdfquery");
var T = rdfquery.T;
var validator = require("./src/shacl-validator");
var ValidationFunction = require("./src/validation-function");
var ValidationEngine = require("./src/validation-engine");
var ValidationReport = require("./src/validation-report");
var debug = require("debug")("index");
var error = require("debug")("index::error");

var $rdf = require("rdflib");
var rdflibgraph = require("./src/rdflib-graph");
var RDFLibGraph = rdflibgraph.RDFLibGraph;
var shapesStore = $rdf.graph();

var common = require("./src/common");
var $shapes = common.$shapes;
$shapes(new RDFLibGraph(shapesStore));
var $data = common.$data;

// invoking this just for the side effects.
// It will trigger the registration of DASH functions
var registerDASH = require("./src/dash").registerDASH;
// registering DASH
registerDASH(ValidationFunction.functionRegistry, common);

/********************************/
/* Vocabularies                 */
/********************************/
var vocabs = require("./src/vocabularies");
var shapesGraphURI = "urn:x-shacl:shapesGraph";
var shaclFile = vocabs.shacl;
var dashFile = vocabs.dash;
/********************************/
/********************************/

// List utility

var createRDFListNode = function(store, items, index) {
    if (index >= items.length) {
        return T("rdf:nil");
    }
    else {
        var bnode = rdfquery.TermFactory.blankNode();
        store.add(bnode, T("rdf:first"), items[index]);
        store.add(bnode, T("rdf:rest"), createRDFListNode(store, items, index + 1));
        return bnode;
    }
};

// Data graph and Shapes graph logic

var results = null;
var dataStore = $rdf.graph();
var validationEngine = null;
var validationError = null;
var sequence = null;
var shapesGraph = new validator.ShapesGraph();

var parseDataGraph = function(text, mediaType, andThen) {
    var dataGraphURI = "urn:x-shacl:dataGraph";
    var newStore = $rdf.graph();
    rdflibgraph.loadGraph(text, newStore, dataGraphURI, mediaType, function () {
        dataStore = newStore;
        $data(new RDFLibGraph(dataStore));
        andThen();
    }, function (ex) {
        error(ex);
    });
};

/**
 * Validates the data graph against the shapes graph using the validation engine
 */
var updateValidationEngine = function() {
    results = [];
    validationEngine = new ValidationEngine(shapesGraph, shapesStore);
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
};

/**
 * Checks for a validation error or results in the validation
 * engine to buid the RDF graph with the validation report
  */
var showValidationResults = function(cb) {
    if (validationError) {
        error("VALIDATION FAILURE: " + validationError);
        throw (validationError);
    }
    else {

        var resultGraph = $rdf.graph();
        var reportNode = rdfquery.TermFactory.blankNode("report");
        resultGraph.add(reportNode, T("rdf:type"), T("sh:ValidationReport"));
        resultGraph.add(reportNode, T("sh:conforms"), T("" + (validationEngine.results.length == 0)));
        var nodes = {};

        for (var i = 0; i < validationEngine.results.length; i++) {
            var result = validationEngine.results[i];
            if (nodes[result[0].toString()] == null) {
                nodes[result[0].toString()] = true;
                resultGraph.add(reportNode, T("sh:result"), result[0]);
            }
            resultGraph.add(result[0], result[1], result[2]);
        }

        // Unsupported bug in JSON parser bug workaround
        var oldToString = resultGraph.toString;
        resultGraph.toString = function () {
            var text = oldToString.call(resultGraph);
            text = text.replace(/^\{/, "").replace(/\}$/, "");
            return text;
        };
        //////////////////

        jsonld.fromRDF(resultGraph.toNT(), {}, function (err, doc) {
            if (err != null) {
                cb(err);
            } else {
                jsonld.flatten(doc, function (err, result) {
                    if (err != null) {
                        cb(err);
                    } else {
                        cb(null, new ValidationReport(result));
                    }
                });
            }
        });
    }
};

/**
 * Reloads the shapes graph.
 * It will load SHACL and DASH shapes constraints.
 */
var parseShapesGraph = function(text, mediaType, andThen) {
    var handleError = function (ex) {
        error(ex);
    };
    var newShapesStore = $rdf.graph();
    rdflibgraph.loadGraph(text, newShapesStore, shapesGraphURI, mediaType, function () {
        rdflibgraph.loadGraph(shaclFile, newShapesStore, "http://shacl.org", "text/turtle", function () {
            rdflibgraph.loadGraph(dashFile, newShapesStore, "http://datashapes.org/dash", "text/turtle", function () {
                shapesStore = newShapesStore;
                $shapes(new RDFLibGraph(shapesStore));
                andThen();
            });
        }, handleError);
    }, handleError);
};

// Update validations

var updateDataGraph = function(text, mediaType, cb) {
    var startTime = new Date().getTime();
    parseDataGraph(text, mediaType, function () {
        var midTime = new Date().getTime();
        updateValidationEngine();
        var endTime = new Date().getTime();
        debug("Parsing took " + (midTime - startTime) + " ms. Validating the data took " + (endTime - midTime) + " ms.");
        try {
            showValidationResults(cb);
        } catch (e) {
            cb(e, null);
        }
    });
};

var updateShapesGraph = function(shapes, mediaType, cb) {
    var startTime = new Date().getTime();
    parseShapesGraph(shapes, mediaType, function () {
        var midTime = new Date().getTime();
        shapesGraph = new validator.ShapesGraph();
        var midTime2 = new Date().getTime();
        updateValidationEngine();
        var endTime = new Date().getTime();
        debug("Parsing took " + (midTime - startTime) + " ms. Preparing the shapes took " + (midTime2 - midTime) + " ms. Validation the data took " + (endTime - midTime2) + " ms.");
        try {
            showValidationResults(cb);
        } catch (e) {
            cb(e, null);
        }
    });
};

/**
 * Validates the provided data graph against the provided shapes graph
 */
module.exports.validate = function (data, dataMediaType, shapes, shapesMediaType, cb) {
    updateDataGraph(data, dataMediaType, function (e) {
        if (e != null) {
            cb(e, null);
        } else {
            updateShapesGraph(shapes, shapesMediaType, function (e, result) {
                if (e) {
                    cb(e, null);
                } else {
                    cb(null, result);
                }
            });
        }
    });
};
