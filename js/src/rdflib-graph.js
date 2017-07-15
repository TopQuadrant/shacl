var $rdf = require("rdflib");
var rdfquery = require("./rdfquery");
var T = rdfquery.T;

var errorHandler = require("debug")("rdflib-graph::error");


// Monkey Patching rdflib, Literals, BlankNodes and NamedNodes
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



var RDFLibGraph = function (store) {
    this.store = store;
};

RDFLibGraph.prototype.find = function (s, p, o) {
    return new RDFLibGraphIterator(this.store, s, p, o);
};

RDFLibGraph.prototype.query = function () {
    return rdfquery(this);
};



var RDFLibGraphIterator = function (store, s, p, o) {
    this.index = 0;
    this.ss = store.statementsMatching(s, p, o);
};

RDFLibGraphIterator.prototype.close = function () {
    // Do nothing
};

RDFLibGraphIterator.prototype.next = function () {
    if (this.index >= this.ss.length) {
        return null;
    }
    else {
        return this.ss[this.index++];
    }
};

function ensureBlankId(component) {
    if (component.termType === "BlankNode") {
        if (typeof(component.value) !== "string") {
            component.value = "_:" + component.id;
        }
        return component;
    }

    return component
}

function postProcessGraph(store, graphURI, newStore) {

    var ss = newStore.statementsMatching(undefined, undefined, undefined);
    for (var i = 0; i < ss.length; i++) {
        var object = ss[i].object;
        ensureBlankId(ss[i].subject);
        ensureBlankId(ss[i].predicate);
        ensureBlankId(ss[i].object);
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


var loadGraph = function(str, store, graphURI, mimeType, andThen, handleError) {
    var newStore = $rdf.graph();
    handleError = handleError || errorHandler;
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
};

module.exports.RDFLibGraph = RDFLibGraph;
module.exports.RDFLibGraphIterator = RDFLibGraphIterator;
module.exports.loadGraph = loadGraph;