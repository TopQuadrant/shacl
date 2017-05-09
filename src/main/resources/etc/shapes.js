/**
 * Created by antoniogarrote on 08/05/2017.
 */
$rdf = require("rdflib");
//$rdf = require("./playgroundrdflib");
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
/* Examples                     */
/********************************/
var examples = {

    amf1: {
        dataFormat: "application/ld+json",
        data: '{"http://raml.org/vocabularies/shapes#toValidate": {\
          "http://raml.org/vocabularies/shapes/anon#title": ["hey","ho"],\
          "http://raml.org/vocabularies/shapes/anon#artist": "Antonio Carlos Brasileiro de Almeida Jobim"\
          } }',
        shapesFormat: "application/ld+json",
        shapes: '{\
        "@context":  {\
          "raml-doc": "http://raml.org/vocabularies/document#",\
          "raml-http": "http://raml.org/vocabularies/http#",\
          "raml-shapes": "http://raml.org/vocabularies/shapes#",\
          "hydra": "http://www.w3.org/ns/hydra/core#",\
          "shacl": "http://www.w3.org/ns/shacl#",\
          "schema-org": "http://schema.org/",\
          "xsd": "http://www.w3.org/2001/XMLSchema#"\
        },\
        "@id": "https://mulesoft-labs.github.io/amf-playground/raml/world-music-api/api.raml#/definitions/Entry/items/0",\
        "@type": [\
            "shacl:NodeShape",\
            "shacl:Shape"\
        ],\
        "shacl:targetObjectsOf": {"@id": "raml-shapes:toValidate"},\
        "shacl:property": [\
            {\
                "@id": "https://mulesoft-labs.github.io/amf-playground/raml/world-music-api/api.raml#/definitions/Entry/items/0/property/title",\
                "@type": [\
                    "shacl:PropertyShape",\
                    "shacl:Shape"\
                ],\
                "raml-shapes:propertyLabel": "title",\
                "shacl:dataType": {\
                    "@id": "xsd:string"\
                },\
                "shacl:maxCount": 1,\
                "shacl:minCount": 0,\
                "shacl:path": {\
                    "@id": "http://raml.org/vocabularies/shapes/anon#title"\
                }\
            },\
            {\
                "@id": "https://mulesoft-labs.github.io/amf-playground/raml/world-music-api/api.raml#/definitions/Entry/items/0/property/artist",\
                "@type": [\
                    "shacl:PropertyShape",\
                    "shacl:Shape"\
                ],\
                "raml-shapes:propertyLabel": "artist",\
                "shacl:dataType": {\
                    "@id": "xsd:string"\
                },\
                "shacl:maxCount": 1,\
                "shacl:minCount": 0,\
                "shacl:path": {\
                    "@id": "http://raml.org/vocabularies/shapes/anon#artist"\
                }\
            }\
        ]\
    }'
    },

    personsTTL: {
        dataFormat: "text/turtle",
        data: '@prefix ex: <http://example.org/ns#> .\n\
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n\
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\
@prefix schema: <http://schema.org/> .\n\
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n\
\n\
ex:Bob\n\
    a schema:Person ;\n\
    schema:givenName "Robert" ;\n\
    schema:familyName "Junior" ;\n\
    schema:birthDate "1971-07-07"^^xsd:date ;\n\
    schema:deathDate "1968-09-10"^^xsd:date ;\n\
    schema:address ex:BobsAddress .\n\
\n\
ex:BobsAddress\n\
    schema:streetAddress "1600 Amphitheatre Pkway" ;\n\
    schema:postalCode 9404 .',
        shapesFormat: "text/turtle",
        shapes: '@prefix dash: <http://datashapes.org/dash#> .\n\
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n\
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n\
@prefix schema: <http://schema.org/> .\n\
@prefix sh: <http://www.w3.org/ns/shacl#> .\n\
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n\
\n\
schema:PersonShape\n\
    a sh:NodeShape ;\n\
    sh:targetClass schema:Person ;\n\
    sh:property [\n\
        sh:path schema:givenName ;\n\
        sh:datatype xsd:string ;\n\
        sh:name "given name" ;\n\
    ] ;\n\
    sh:property [\n\
        sh:path schema:birthDate ;\n\
        sh:lessThan schema:deathDate ;\n\
        sh:maxCount 1 ;\n\
    ] ;\n\
    sh:property [\n\
        sh:path schema:gender ;\n\
        sh:in ( "female" "male" ) ;\n\
    ] ;\n\
    sh:property [\n\
        sh:path schema:address ;\n\
        sh:node schema:AddressShape ;\n\
    ] .\n\
\n\
schema:AddressShape\n\
    a sh:NodeShape ;\n\
    sh:closed true ;\n\
    sh:property [\n\
        sh:path schema:streetAddress ;\n\
        sh:datatype xsd:string ;\n\
    ] ;\n\
    sh:property [\n\
        sh:path schema:postalCode ;\n\
        sh:or ( [ sh:datatype xsd:string ] [ sh:datatype xsd:integer ] ) ;\n\
        sh:minInclusive 10000 ;\n\
        sh:maxInclusive 99999 ;\n\
    ] .'
    },

    personsJSON: {

        data: '{\n\
    "@context": { "@vocab": "http://schema.org/" },\n\
\n\
    "@id": "http://example.org/ns#Bob",\n\
    "@type": "Person",\n\
    "givenName": "Robert",\n\
    "familyName": "Junior",\n\
    "birthDate": "1971-07-07",\n\
    "deathDate": "1968-09-10",\n\
    "address": {\n\
        "@id": "http://example.org/ns#BobsAddress",\n\
        "streetAddress": "1600 Amphitheatre Pkway",\n\
        "postalCode": 9404\n\
    }\n\
}',
        dataFormat: "application/ld+json",
        shapes: '',
        shapesFormat: "text/turtle"
    }
};
examples.personsJSON.shapes = examples.personsTTL.shapes;
/********************************/
/********************************/

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

function validate(data, dataMediaType, shapes, shapesMediaType, cb) {
    updateDataGraph(data, dataMediaType, function () {
        updateShapesGraph(shapes, shapesMediaType, function () {
            console.log("DONE!");
            cb();
        });
    });
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

console.log("TESTING THE THING");
console.log(examples.amf1.shapes);
validate(
    examples.amf1.data,
    examples.amf1.dataFormat,
    examples.amf1.shapes,
    examples.amf1.shapesFormat,
    function () {
        console.log("AND BACK AGAIN");
    });
