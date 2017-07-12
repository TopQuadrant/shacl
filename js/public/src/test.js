// expected result
var $rdf = require("rdflib");
var rdflibgraph = require("../../src/rdflib-graph");
var RDFLibGraph = rdflibgraph.RDFLibGraph;


var ExpectedValidationResult = function(solution) {
    this._id = solution["report"].value;

    this._focusNode = solution["focusNode"].termType === "BlankNode" ? "_:" + solution["focusNode"].id : solution["focusNode"].value;
    this._severity = solution["severity"].value;
    this._constraint = solution["constraint"].value;
    this._shape = solution["shape"].value;
};

ExpectedValidationResult.prototype.id = function() {
    return this._id;
}

ExpectedValidationResult.prototype.focusNode = function() {
    return this._focusNode;
};

ExpectedValidationResult.prototype.severity = function() {
    if (this._severity != null) {
        return this._severity.split("#")[1];
    }
};

ExpectedValidationResult.prototype.sourceConstraintComponent = function() {
    return this._constraint;
};

ExpectedValidationResult.prototype.sourceShape = function() {
    return this._shape
};


var ExpectedValidationReport = function(graph) {
    this.graph = graph;
};

ExpectedValidationReport.prototype.conforms = function() {
    var conforms = this.graph.query()
        .match("?report", "rdf:type", "sh:ValidationReport")
        .match("?report", "sh:conforms", "?conforms")
        .getNode("?conforms");
    return conforms != null && conforms.value === "true"
};

ExpectedValidationReport.prototype.results = function() {
    var acc = [];
    var query = this.graph.query()
        .match("?report", "sh:result", "?result")
        .match("?result", "sh:focusNode", "?focusNode")
        .match("?result", "sh:resultSeverity", "?severity")
        .match("?result", "sh:sourceConstraintComponent", "?constraint")
        .match("?result", "sh:sourceShape", "?shape");
    var solution = query.nextSolution();
    while (solution != null) {
        acc.push(new ExpectedValidationResult(solution));
        solution = query.nextSolution();
    }
    return acc;
};

var expectedResult = function(data, mediaType, cb) {
    var store = $rdf.graph();
    rdflibgraph.loadGraph(data, store, "http://test.com/example", mediaType, function() {
        var graph = new RDFLibGraph(store);
        var expectedValidationReport = new ExpectedValidationReport(graph);
        expectedValidationReport.results();
        cb(expectedValidationReport, null);
    }, function(e) {
        cb(null, e);
    });
};

var isBlank = function(s) {
    return s != null && (s.indexOf("_:") === 0 || s.indexOf("_g_") > -1);
}

var validateReports = function(test, done, data) {

    expectedResult(data, "text/turtle", function(expectedReport, e) {
        if (e != null) {
            test.ok(e != null);
            done();
        } else {
            new SHACLValidator().validate(data, "text/turtle", data, "text/turtle", function (e, report) {
                if (e != null) {
                    test.ok(e != null);
                    done();
                } else {
                    test.ok(report.conforms() === expectedReport.conforms());
                    test.ok(report.results().length === expectedReport.results().length);
                    var results = report.results() || [];
                    var expectedResults = expectedReport.results();
                    for (var i=0; i <results.length; i++) {
                        found = false;
                        for (var j=0; j<expectedResults.length; j++) {
                            if (//(results[i].focusNode() ===  expectedResults[j].focusNode() ) &&
                            results[i].severity() === expectedResults[j].severity() &&
                            ( (isBlank(results[i].sourceShape()) && isBlank(expectedResults[j].sourceShape())) ||
                            results[i].sourceShape() === expectedResults[j].sourceShape()) &&
                            results[i].sourceConstraintComponent() === expectedResults[j].sourceConstraintComponent()) {
                                found = true;
                            }

                        }
                        test.ok(found === true);
                    }
                    done();
                }
            });
        }
    });
};

var loadTestCases = function(k) {
    var oReq = new XMLHttpRequest();
    oReq.addEventListener("load", function() {
        var cases = JSON.parse(this.responseText);
        k(cases);
    });
    oReq.open("GET", "test_cases.json");
    oReq.send();
};

var loadTestCase = function(testCase, k) {
    var oReq = new XMLHttpRequest();
    oReq.addEventListener("load", function() {
        k(this.responseText);
    });
    oReq.open("GET", testCase);
    oReq.send();
};

loadTestCases(function(testCases) {
    for (var i=0; i<testCases.length; i++) {
        (function(file) {
            QUnit.test("Test case " + file, function(assert) {
                var done = assert.async();
                loadTestCase(file, function(data) {
                    validateReports(assert, done, data);
                });
            });
        })(testCases[i])
    }
});


/*


QUnit.test("Integration test 2", function (assert) {
    var done = assert.async();
    new SHACLValidator().validate(
        examples.example2.data,
        examples.example2.dataFormat,
        examples.example2.shapes,
        examples.example2.shapesFormat,
        function (e, report) {
            if (e != null) {
                console.log(e);
            }
            assert.ok(e == null);
            assert.ok(!report.conforms());
            var results = report.results();
            assert.ok(results.length === 1);
            assert.ok(results[0].message() === "More than 1 values");
            assert.ok(results[0].path() === "http://raml.org/vocabularies/shapes/anon#title");
            assert.ok(results[0].focusNode() !== null);
            assert.ok(results[0].severity() === "Violation");
            assert.ok(results[0].sourceConstraintComponent() === "http://www.w3.org/ns/shacl#MaxCountConstraintComponent");
            assert.ok(results[0].sourceShape() === "https://mulesoft-labs.github.io/amf-playground/raml/world-music-api/api.raml#/definitions/Entry/items/0/property/title");
            done();
        });
});

*/