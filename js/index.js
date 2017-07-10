/**
 * Created by antoniogarrote on 08/05/2017.
 */

var jsonld = require("jsonld");
var T = require("./src/rdfquery").T;
var TermFactory = require("./src/rdfquery/term-factory");
var ShapesGraph = require("./src/shapes-graph");
var ValidationEngine = require("./src/validation-engine");
var ValidationReport = require("./src/validation-report");
var debug = require("debug")("index");
var error = require("debug")("index::error");

var $rdf = require("rdflib");
var rdflibgraph = require("./src/rdflib-graph");
var RDFLibGraph = rdflibgraph.RDFLibGraph;



/********************************/
/* Vocabularies                 */
/********************************/
var vocabs = require("./src/vocabularies");
var shapesGraphURI = "urn:x-shacl:shapesGraph";
var dataGraphURI = "urn:x-shacl:dataGraph";
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
        var bnode = TermFactory.blankNode();
        store.add(bnode, T("rdf:first"), items[index]);
        store.add(bnode, T("rdf:rest"), createRDFListNode(store, items, index + 1));
        return bnode;
    }
};


/**
 * SHACL Validator.
 * Main interface with the library
 */
var SHACLValidator = function() {
    this.functionRegistry = {};
    // invoking this just for the side effects.
    // It will trigger the registration of DASH functions
    require("./src/dash").registerDASH(this);

    this.results = null;
    this.dataStore = $rdf.graph();
    this.$data = new RDFLibGraph(this.dataStore);
    this.$shapes = new RDFLibGraph($rdf.graph());
    this.validationEngine = null;
    this.validationError = null;
    this.sequence = null;
    this.shapesGraph = new ShapesGraph(this);
};



// Data graph and Shapes graph logic


SHACLValidator.prototype.parseDataGraph = function(text, mediaType, andThen) {
    this.dataStore = $rdf.graph();
    var that = this;
    rdflibgraph.loadGraph(text, this.dataStore, dataGraphURI, mediaType, function () {
        that.$data = new RDFLibGraph(that.dataStore);
        andThen();
    }, function (ex) {
        error(ex);
    });
};

/**
 * Validates the data graph against the shapes graph using the validation engine
 */
SHACLValidator.prototype.updateValidationEngine = function() {
    results = [];
    this.validationEngine = new ValidationEngine(this);
    try {
        this.validationError = null;
        if (this.sequence) {
            this.sequence = [];
        }
        this.validationEngine.validateAll(this.$data);
    }
    catch (ex) {
        this.validationError = ex;
    }
};

/**
 * Checks for a validation error or results in the validation
 * engine to build the RDF graph with the validation report.
 * It returns a ValidationReport object wrapping the RDF graph
 */
SHACLValidator.prototype.showValidationResults = function(cb) {
    if (this.validationError) {
        error("Validation Failure: " + this.validationError);
        throw (this.validationError);
    }
    else {

        var resultGraph = $rdf.graph();
        var reportNode = TermFactory.blankNode("report");
        resultGraph.add(reportNode, T("rdf:type"), T("sh:ValidationReport"));
        resultGraph.add(reportNode, T("sh:conforms"), T("" + (this.validationEngine.results.length == 0)));
        var nodes = {};

        for (var i = 0; i < this.validationEngine.results.length; i++) {
            var result = this.validationEngine.results[i];
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
SHACLValidator.prototype.parseShapesGraph = function(text, mediaType, andThen) {
    var handleError = function (ex) {
        error(ex);
    };
    this.shapesStore = $rdf.graph();
    var that = this;
    rdflibgraph.loadGraph(text, this.shapesStore, shapesGraphURI, mediaType, function () {
        rdflibgraph.loadGraph(shaclFile, that.shapesStore, "http://shacl.org", "text/turtle", function () {
            rdflibgraph.loadGraph(dashFile, that.shapesStore, "http://datashapes.org/dash", "text/turtle", function () {
                that.$shapes = new RDFLibGraph(that.shapesStore);
                andThen();
            });
        }, handleError);
    }, handleError);
};


// Update validations

/**
 * Updates the data graph and validate it against the current data shapes
 */
SHACLValidator.prototype.updateDataGraph = function(text, mediaType, cb) {
    var startTime = new Date().getTime();
    var that = this;
    this.parseDataGraph(text, mediaType, function () {
        var midTime = new Date().getTime();
        that.updateValidationEngine();
        var endTime = new Date().getTime();
        debug("Parsing took " + (midTime - startTime) + " ms. Validating the data took " + (endTime - midTime) + " ms.");
        try {
            that.showValidationResults(cb);
        } catch (e) {
            cb(e, null);
        }
    });
};

/**
 *  *pdates the shapes graph and validates it against the current data graph
 */
SHACLValidator.prototype.updateShapesGraph = function(shapes, mediaType, cb) {
    var startTime = new Date().getTime();
    var that = this;
    this.parseShapesGraph(shapes, mediaType, function () {
        var midTime = new Date().getTime();
        that.shapesGraph = new ShapesGraph(that);
        var midTime2 = new Date().getTime();
        that.updateValidationEngine();
        var endTime = new Date().getTime();
        debug("Parsing took " + (midTime - startTime) + " ms. Preparing the shapes took " + (midTime2 - midTime)
            + " ms. Validation the data took " + (endTime - midTime2) + " ms.");
        try {
            that.showValidationResults(cb);
        } catch (e) {
            cb(e, null);
        }
    });
};

/**
 * Validates the provided data graph against the provided shapes graph
 */
SHACLValidator.prototype.validate = function (data, dataMediaType, shapes, shapesMediaType, cb) {
    var that = this;
    this.updateDataGraph(data, dataMediaType, function (e) {
        if (e != null) {
            cb(e, null);
        } else {
            that.updateShapesGraph(shapes, shapesMediaType, function (e, result) {
                if (e) {
                    cb(e, null);
                } else {
                    cb(null, result);
                }
            });
        }
    });
};

module.exports = SHACLValidator;
