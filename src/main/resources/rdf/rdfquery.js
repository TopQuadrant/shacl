// rdfquery.js
// A simple RDF query library for JavaScript
//
// Contact: Holger Knublauch, TopQuadrant, Inc. (holger@topquadrant.com)
//
// The basic idea is that the function RDFQuery produces an initial
// Query object, which starts with a single "empty" solution.
// Each query object has a function nextSolution() producing an iteration
// of variable bindings ("volcano style").
// Each query object can be refined with subsequent calls to other
// functions, producing new queries.
// Invoking nextSolution on a query will pull solutions from its
// predecessors in a chain of query objects.
// The solution objects are plain JavaScript objects providing a
// mapping from variable names to RDF Term objects.
// Unless a query has been walked to exhaustion, .close() must be called.
//
// Finally, terminal functions such as .getNode() and .getArray() can be used
// to produce individual values.  All terminal functions close the query.
//
// RDF Term/Node objects are expected to follow the contracts from the
// RDF Representation Task Force's interface specification:
// https://github.com/rdfjs/representation-task-force/blob/master/interface-spec.md
//
// In order to bootstrap all this, graph objects need to implement a
// function .find(s, p, o) where each parameter is either an RDF term or null
// producing an iterator object with a .next() function that produces RDF triples
// (with attributes subject, predicate, object) or null when done.
//
// (Note I am not particularly a JavaScript guru so the modularization of this
// script may be improved to hide private members from public API etc).

/*
Example:

	var result = $data.query().
		match("owl:Class", "rdfs:label", "?label").
		match("?otherClass", "rdfs:label", "?label").
		filter(function(sol) { return !T("owl:Class").equals(sol.otherClass) }).
		getNode("?otherClass");

Equivalent SPARQL:
		SELECT ?otherClass
		WHERE {
			owl:Class rdfs:label ?label .
			?otherClass rdfs:label ?label .
			FILTER (owl:Class != ?otherClass) .
		} LIMIT 1
*/

if(!this["TermFactory"]) {
    // In some environments such as Nashorn this may already have a value
    // In TopBraid this is redirecting to native Jena calls
    TermFactory = {

        REGEX_URI: /^([a-z][a-z0-9+.-]*):(?:\/\/((?:(?=((?:[a-z0-9-._~!$&'()*+,;=:]|%[0-9A-F]{2})*))(\3)@)?(?=(\[[0-9A-F:.]{2,}\]|(?:[a-z0-9-._~!$&'()*+,;=]|%[0-9A-F]{2})*))\5(?::(?=(\d*))\6)?)(\/(?=((?:[a-z0-9-._~!$&'()*+,;=:@\/]|%[0-9A-F]{2})*))\8)?|(\/?(?!\/)(?=((?:[a-z0-9-._~!$&'()*+,;=:@\/]|%[0-9A-F]{2})*))\10)?)(?:\?(?=((?:[a-z0-9-._~!$&'()*+,;=:@\/?]|%[0-9A-F]{2})*))\11)?(?:#(?=((?:[a-z0-9-._~!$&'()*+,;=:@\/?]|%[0-9A-F]{2})*))\12)?$/i,

        impl : null,   // This needs to be connected to an API such as $rdf

        // Globally registered prefixes for TTL short cuts
        namespaces : {},

        /**
         * Registers a new namespace prefix for global TTL short cuts (qnames).
         * @param prefix  the prefix to add
         * @param namespace  the namespace to add for the prefix
         */
        registerNamespace : function(prefix, namespace) {
            if(this.namespaces.prefix) {
                throw "Prefix " + prefix + " already registered"
            }
            this.namespaces[prefix] = namespace;
        },

        /**
         * Produces an RDF term from a TTL string representation.
         * Also uses the registered prefixes.
         * @param str  a string, e.g. "owl:Thing" or "true" or '"Hello"@en'.
         * @return an RDF term
         */
        term : function(str) {
            // TODO: this implementation currently only supports booleans and qnames - better overload to rdflib.js
            if ("true" === str || "false" === str) {
                return this.literal(str, (this.term("xsd:boolean")));
            }

            if (str.match(/^\d+$/)) {
                return this.literal(str, (this.term("xsd:integer")));
            }

            if (str.match(/^\d+\.\d+$/)) {
                return this.literal(str, (this.term("xsd:float")));
            }

            var col = str.indexOf(":");
            if (col > 0) {
                var ns = this.namespaces[str.substring(0, col)];
                if (ns != null) {
                    return this.namedNode(ns + str.substring(col + 1));
                } else {
                    if (str.match(REGEX_URI)) {
                        return this.namedNode(str)
                    }
                }
            }
            return this.literal(str);
        },

        /**
         * Produces a new blank node.
         * @param id  an optional ID for the node
         */
        blankNode : function(id) {
            return this.impl.blankNode(id);
        },

        /**
         * Produces a new literal.  For example .literal("42", T("xsd:integer")).
         * @param lex  the lexical form, e.g. "42"
         * @param langOrDatatype  either a language string or a URI node with the datatype
         */
        literal : function(lex, langOrDatatype) {
            return this.impl.literal(lex, langOrDatatype)
        },

        // This function is basically left for Task Force compatibility, but the preferred function is uri()
        namedNode : function(uri) {
            return this.impl.namedNode(uri)
        },

        /**
         * Produces a new URI node.
         * @param uri  the URI of the node
         */
        uri : function(uri) {
            return namedNode(uri);
        }
    }
}

// Install NodeFactory as an alias - unsure which name is best long term:
// The official name in RDF is "term", while "node" is more commonly understood.
// Oficially, a "node" must be in a graph though, while "terms" are independent.
var NodeFactory = TermFactory;


NodeFactory.registerNamespace("dc", "http://purl.org/dc/elements/1.1/")
NodeFactory.registerNamespace("dcterms", "http://purl.org/dc/terms/")
NodeFactory.registerNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
NodeFactory.registerNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
NodeFactory.registerNamespace("schema", "http://schema.org/")
NodeFactory.registerNamespace("sh", "http://www.w3.org/ns/shacl#")
NodeFactory.registerNamespace("skos", "http://www.w3.org/2004/02/skos/core#")
NodeFactory.registerNamespace("owl", "http://www.w3.org/2002/07/owl#")
NodeFactory.registerNamespace("xsd", "http://www.w3.org/2001/XMLSchema#")

// Candidates:
// NodeFactory.registerNamespace("prov", "http://www.w3.org/ns/prov#");

/**
 * A shortcut for NodeFactory.term(str) - turns a TTL string representation of an RDF
 * term into a proper RDF term.
 * This will also use the globally registered namespace prefixes.
 * @param str  the string representation, e.g. "owl:Thing"
 * @returns
 */
function T(str) {
    return NodeFactory.term(str)
}


/**
 * Creates a query object for a given graph and optional initial solution.
 * The resulting object can be further refined using the functions on
 * AbstractQuery such as <code>match()</code> and <code>filter()</code>.
 * Functions such as <code>nextSolution()</code> can be used to get the actual results.
 * @param graph  the graph to query
 * @param initialSolution  the initial solutions or null for none
 * @returns a query object
 */
function RDFQuery(graph, initialSolution) {
    return new StartQuery(graph, initialSolution ? initialSolution : []);
}


// class AbstractQuery

function AbstractQuery() {
}

// ----------------------------------------------------------------------------
// Query constructor functions, can be chained together
// ----------------------------------------------------------------------------

/**
 * Creates a new query that adds a binding for a given variable into
 * each solution produced by the input query.
 * @param varName  the name of the variable to bind, starting with "?"
 * @param bindFunction  a function that takes a solution object
 *                      and returns a node or null based on it.
 */
AbstractQuery.prototype.bind = function(varName, bindFunction) {
    return new BindQuery(this, varName, bindFunction);
}

/**
 * Creates a new query that filters the solutions produced by this.
 * @param filterFunction  a function that takes a solution object
 *                        and returns true iff that solution is valid
 */
AbstractQuery.prototype.filter = function(filterFunction) {
    return new FilterQuery(this, filterFunction);
}

/**
 * Creates a new query that only allows the first n solutions through.
 * @param limit  the maximum number of results to allow
 */
AbstractQuery.prototype.limit = function(limit) {
    return new LimitQuery(this, limit);
}

/**
 * Creates a new query doing a triple match.
 * In each subject, predicate, object position, the values can either be
 * an RDF term object or null (wildcard) or a string.
 * If it is a string it may either be a variable (starting with "?")
 * or the TTL representation of an RDF term using the T() function.
 * @param s  the match subject
 * @param p  the match predicate
 * @param o  the match object
 */
AbstractQuery.prototype.match = function(s, p, o) {
    return new MatchQuery(this, s, p, o);
}

/**
 * Creates a new query that sorts all input solutions by the bindings
 * for a given variable.
 * @param varName  the name of the variable to sort by, starting with "?"
 */
AbstractQuery.prototype.orderBy = function(varName) {
    return new OrderByQuery(this, varName);
}

/**
 * Creates a new query doing a match where the predicate may be a RDF Path object.
 * Note: This is currently not using lazy evaluation and will always walk all matches.
 * Path syntax:
 * - PredicatePaths: NamedNode
 * - SequencePaths: [path1, path2]
 * - AlternativePaths: { or : [ path1, path2 ] }
 * - InversePaths: { inverse : path }   LIMITATION: Only supports NamedNodes for path here
 * - ZeroOrMorePaths: { zeroOrMore : path }
 * - OneOrMorePaths: { oneOrMore : path }
 * - ZeroOrOnePaths: { zeroOrOne : path }
 * @param s  the match subject or a variable name (string) - must have a value
 *           at execution time!
 * @param path  the match path object (e.g. a NamedNode for a simple predicate hop)
 * @param o  the match object or a variable name (string)
 */
AbstractQuery.prototype.path = function(s, path, o) {
    if(path && path.value && path.isURI()) {
        return new MatchQuery(this, s, path, o);
    }
    else {
        return new PathQuery(this, s, path, o);
    }
}

// TODO: add other SPARQL-like query types
//       - .distinct()
//       - .union(otherQuery)


// ----------------------------------------------------------------------------
// Terminal functions - convenience functions to get values.
// All these functions close the solution iterators.
// ----------------------------------------------------------------------------

/**
 * Adds all nodes produced by a given solution variable into a set.
 * The set must have an add(node) function.
 * @param varName  the name of the variable, starting with "?"
 * @param set  the set to add to
 */
AbstractQuery.prototype.addAllNodes = function(varName, set) {
    var attrName = var2Attr(varName);
    for(var sol = this.nextSolution(); sol; sol = this.nextSolution()) {
        var node = sol[attrName];
        if(node) {
            set.add(node);
        }
    }
}

/**
 * Produces an array of triple objects where each triple object has properties
 * subject, predicate and object derived from the provided template values.
 * Each of these templates can be either a variable name (starting with '?'),
 * an RDF term string (such as "rdfs:label") or a JavaScript node object.
 * @param subject  the subject node
 * @param predicate  the predicate node
 * @param object  the object node
 */
AbstractQuery.prototype.construct = function(subject, predicate, object) {
    var results = [];
    for(var sol = this.nextSolution(); sol; sol = this.nextSolution()) {
        var s = null;
        if(typeof subject === 'string') {
            if(subject.indexOf('?') == 0) {
                s = sol[var2Attr(subject)];
            }
            else {
                s = T(subject);
            }
        }
        else {
            s = subject;
        }
        var p = null;
        if(typeof predicate === 'string') {
            if(predicate.indexOf('?') == 0) {
                p = sol[var2Attr(predicate)];
            }
            else {
                p = T(predicate);
            }
        }
        else {
            p = predicate;
        }

        var o = null;
        if(typeof object === 'string') {
            if(object.indexOf('?') == 0) {
                o = sol[var2Attr(object)];
            }
            else {
                o = T(object);
            }
        }
        else {
            o = object;
        }

        if(s && p && o) {
            results.push({ subject: s, predicate: p, object: o});
        }
    }
    return results;
}

/**
 * Executes a given function for each solution.
 * @param callback  a function that takes a solution as argument
 */
AbstractQuery.prototype.forEach = function(callback) {
    for(var n = this.nextSolution(); n; n = this.nextSolution()) {
        callback(n);
    }
}

/**
 * Executes a given function for each node in a solution set.
 * @param varName  the name of a variable, starting with "?"
 * @param callback  a function that takes a node as argument
 */
AbstractQuery.prototype.forEachNode = function(varName, callback) {
    var attrName = var2Attr(varName);
    for(var sol = this.nextSolution(); sol; sol = this.nextSolution()) {
        var node = sol[attrName];
        if(node) {
            callback(node);
        }
    }
}

/**
 * Turns all result solutions into an array.
 * @return an array consisting of solution objects
 */
AbstractQuery.prototype.getArray = function() {
    var results = [];
    for(var n = this.nextSolution(); n != null; n = this.nextSolution()) {
        results.push(n);
    }
    return results;
}

/**
 * Gets the number of (remaining) solutions.
 * @return the count
 */
AbstractQuery.prototype.getCount = function() {
    return this.getArray().length; // Quick and dirty implementation
}

/**
 * Gets the next solution and, if that exists, returns the binding for a
 * given variable from that solution.
 * @param varName  the name of the binding to get, starting with "?"
 * @return the value of the variable or null or undefined if it doesn't exist
 */
AbstractQuery.prototype.getNode = function(varName) {
    var s = this.nextSolution();
    if(s) {
        this.close();
        return s[var2Attr(varName)];
    }
    else {
        return null;
    }
}

/**
 * Turns all results into an array of bindings for a given variable.
 * @return an array consisting of RDF node objects
 */
AbstractQuery.prototype.getNodeArray = function(varName) {
    var results = [];
    var attr = var2Attr(varName);
    for(var n = this.nextSolution(); n != null; n = this.nextSolution()) {
        results.push(n[attr]);
    }
    return results;
}

/**
 * Turns all result bindings for a given variable into a set.
 * The set has functions .contains and .toArray.
 * @param varName  the name of the variable, starting with "?"
 * @return a set consisting of RDF node objects
 */
AbstractQuery.prototype.getNodeSet = function(varName) {
    var results = new NodeSet();
    var attr = var2Attr(varName);
    for(var n = this.nextSolution(); n != null; n = this.nextSolution()) {
        results.add(n[attr]);
    }
    return results;
}

/**
 * Queries the underlying graph for the object of a subject/predicate combination,
 * where either subject or predicate can be a variable which is substituted with
 * a value from the next input solution.
 * Note that even if there are multiple solutions it will just return the "first"
 * one and since the order of triples in RDF is undefined this may lead to random results.
 * Unbound values produce errors.
 * @param subject  an RDF term or a variable (starting with "?") or a TTL representation
 * @param predicate  an RDF term or a variable (starting with "?") or a TTL representation
 * @return the object of the "first" triple matching the subject/predicate combination
 */
AbstractQuery.prototype.getObject = function(subject, predicate) {
    var sol = this.nextSolution();
    if(sol) {
        this.close();
        var s;
        if(typeof subject === 'string') {
            if(subject.indexOf('?') == 0) {
                s = sol[var2Attr(subject)];
            }
            else {
                s = T(subject);
            }
        }
        else {
            s = subject;
        }
        if(!s) {
            throw "getObject() called with null subject";
        }
        var p;
        if(typeof predicate === 'string') {
            if(predicate.indexOf('?') == 0) {
                p = sol[var2Attr(predicate)];
            }
            else {
                p = T(predicate);
            }
        }
        else {
            p = predicate;
        }
        if(!p) {
            throw "getObject() called with null predicate";
        }

        var it = this.source.find(s, p, null);
        var triple = it.next();
        if(triple) {
            it.close();
            return triple.object;
        }
    }
    return null;
}

/**
 * Tests if there is any solution and closes the query.
 * @return true if there is another solution
 */
AbstractQuery.prototype.hasSolution = function() {
    if(this.nextSolution()) {
        this.close();
        return true;
    }
    else {
        return false;
    }
}


// ----------------------------------------------------------------------------
// Expression functions - may be used in filter and bind queries
// ----------------------------------------------------------------------------

/**
 * Creates a function that takes a solution and compares a given node with
 * the binding of a given variable from that solution.
 * @param varName  the name of the variable (starting with "?")
 * @param node  the node to compare with
 * @returns true if the solution's variable equals the node
 */
function exprEquals(varName, node) {
    return function(sol) {
        return node.equals(sol[var2Attr(varName)]);
    }
}

/**
 * Creates a function that takes a solution and compares a given node with
 * the binding of a given variable from that solution.
 * @param varName  the name of the variable (starting with "?")
 * @param node  the node to compare with
 * @returns true if the solution's variable does not equal the node
 */
function exprNotEquals(varName, node) {
    return function(sol) {
        return !node.equals(sol[var2Attr(varName)]);
    }
}


// ----------------------------------------------------------------------------
// END OF PUBLIC API ----------------------------------------------------------
// ----------------------------------------------------------------------------


// class BindQuery
// Takes all input solutions but adds a value for a given variable so that
// the value is computed by a given function based on the current solution.
// It is illegal to use a variable that already has a value from the input.

function BindQuery(input, varName, bindFunction) {
    this.attr = var2Attr(varName);
    this.source = input.source;
    this.input = input;
    this.bindFunction = bindFunction;
}

BindQuery.prototype = Object.create(AbstractQuery.prototype);

BindQuery.prototype.close = function() {
    this.input.close();
}

// Pulls the next result from the input Query and passes it into
// the given bind function to add a new node
BindQuery.prototype.nextSolution = function() {
    var result = this.input.nextSolution();
    if(result == null) {
        return null;
    }
    else {
        var newNode = this.bindFunction(result);
        if(newNode) {
            result[this.attr] = newNode;
        }
        return result;
    }
}


// class FilterQuery
// Filters the incoming solutions, only letting through those where
// filterFunction(solution) returns true

function FilterQuery(input, filterFunction) {
    this.source = input.source;
    this.input = input;
    this.filterFunction = filterFunction;
}

FilterQuery.prototype = Object.create(AbstractQuery.prototype);

FilterQuery.prototype.close = function() {
    this.input.close();
}

// Pulls the next result from the input Query and passes it into
// the given filter function
FilterQuery.prototype.nextSolution = function() {
    for(;;) {
        var result = this.input.nextSolution();
        if(result == null) {
            return null;
        }
        else if(this.filterFunction(result) === true) {
            return result;
        }
    }
}


// class LimitQuery
// Only allows the first n values of the input query through

function LimitQuery(input, limit) {
    this.source = input.source;
    this.input = input;
    this.limit = limit;
}

LimitQuery.prototype = Object.create(AbstractQuery.prototype);

LimitQuery.prototype.close = function() {
    this.input.close();
}

// Pulls the next result from the input Query unless the number
// of previous calls has exceeded the given limit
LimitQuery.prototype.nextSolution = function() {
    if(this.limit > 0) {
        this.limit--;
        return this.input.nextSolution();
    }
    else {
        this.input.close();
        return null;
    }
}


// class MatchQuery
// Joins the solutions from the input Query with triple matches against
// the current input graph.

function MatchQuery(input, s, p, o) {
    this.source = input.source;
    this.input = input;
    if(typeof s === 'string') {
        if(s.indexOf('?') == 0) {
            this.sv = var2Attr(s);
        }
        else {
            this.s = T(s);
        }
    }
    else {
        this.s = s;
    }
    if(typeof p === 'string') {
        if(p.indexOf('?') == 0) {
            this.pv = var2Attr(p);
        }
        else {
            this.p = T(p);
        }
    }
    else {
        this.p = p;
    }
    if(typeof o === 'string') {
        if(o.indexOf('?') == 0) {
            this.ov = var2Attr(o);
        }
        else {
            this.o = T(o);
        }
    }
    else {
        this.o = o;
    }
}

MatchQuery.prototype = Object.create(AbstractQuery.prototype);

MatchQuery.prototype.close = function() {
    this.input.close();
    if(this.ownIterator) {
        this.ownIterator.close();
    }
}

// This pulls the first solution from the input Query and uses it to
// create an "ownIterator" which applies the input solution to those
// specified by s, p, o.
// Once this "ownIterator" has been exhausted, it moves to the next
// solution from the input Query, and so on.
// At each step, it produces the union of the input solutions plus the
// own solutions.
MatchQuery.prototype.nextSolution = function() {

    var oit = this.ownIterator;
    if(oit) {
        var n = oit.next();
        if(n != null) {
            var result = createSolution(this.inputSolution);
            if(this.sv) {
                result[this.sv] = n.subject;
            }
            if(this.pv) {
                result[this.pv] = n.predicate;
            }
            if(this.ov) {
                result[this.ov] = n.object;
            }
            return result;
        }
        else {
            delete this.ownIterator; // Mark as exhausted
        }
    }

    // Pull from input
    this.inputSolution = this.input.nextSolution();
    if(this.inputSolution) {
        var sm = this.sv ? this.inputSolution[this.sv] : this.s;
        var pm = this.pv ? this.inputSolution[this.pv] : this.p;
        var om = this.ov ? this.inputSolution[this.ov] : this.o;
        this.ownIterator = this.source.find(sm, pm, om);
        return this.nextSolution();
    }
    else {
        return null;
    }
}


// class OrderByQuery
// Sorts all solutions from the input stream by a given variable

function OrderByQuery(input, varName) {
    this.input = input;
    this.source = input.source;
    this.attrName = var2Attr(varName);
}

OrderByQuery.prototype = Object.create(AbstractQuery.prototype);

OrderByQuery.prototype.close = function() {
    this.input.close();
}

OrderByQuery.prototype.nextSolution = function() {
    if(!this.solutions) {
        this.solutions = this.input.getArray();
        var attrName = this.attrName;
        this.solutions.sort(function(s1, s2) {
            return compareTerms(s1[attrName], s2[attrName]);
        });
        this.index = 0;
    }
    if(this.index < this.solutions.length) {
        return this.solutions[this.index++];
    }
    else {
        return null;
    }
}


// class PathQuery
// Expects subject and path to be bound and produces all bindings
// for the object variable or matches that by evaluating the given path

function PathQuery(input, subject, path, object) {
    this.input = input;
    this.source = input.source;
    if(typeof subject === 'string' && subject.indexOf("?") == 0) {
        this.subjectAttr = var2Attr(subject);
    }
    else {
        this.subject = subject;
    }
    if(path == null) {
        throw "Path cannot be unbound";
    }
    if(typeof path === 'string') {
        this.path_ = T(path);
    }
    else {
        this.path_ = path;
    }
    if(typeof object === 'string' && object.indexOf("?") == 0) {
        this.objectAttr = var2Attr(object);
    }
    else {
        this.object = object;
    }
}

PathQuery.prototype = Object.create(AbstractQuery.prototype);

PathQuery.prototype.close = function() {
    this.input.close();
}

PathQuery.prototype.nextSolution = function() {

    var r = this.pathResults;
    if(r) {
        var n = r[this.pathIndex++];
        var result = createSolution(this.inputSolution);
        if(this.objectAttr) {
            result[this.objectAttr] = n;
        }
        if(this.pathIndex == r.length) {
            delete this.pathResults; // Mark as exhausted
        }
        return result;
    }

    // Pull from input
    this.inputSolution = this.input.nextSolution();
    if(this.inputSolution) {
        var sm = this.subjectAttr ? this.inputSolution[this.subjectAttr] : this.subject;
        if(sm == null) {
            throw "Path cannot have unbound subject";
        }
        var om = this.objectAttr ? this.inputSolution[this.objectAttr] : this.object;
        var pathResultsSet = new NodeSet();
        addPathValues(this.source, sm, this.path_, pathResultsSet);
        this.pathResults = pathResultsSet.toArray();
        if(this.pathResults.length == 0) {
            delete this.pathResults;
        }
        else if(om) {
            delete this.pathResults;
            if(pathResultsSet.contains(om)) {
                return this.inputSolution;
            }
        }
        else {
            this.pathIndex = 0;
        }
        return this.nextSolution();
    }
    else {
        return null;
    }
}


// class StartQuery
// This simply produces a single result: the initial solution

function StartQuery(source, initialSolution) {
    this.source = source;
    if (initialSolution && initialSolution.length > 0) {
        this.solution = initialSolution;
    } else {
        this.solution = [{}];
    }
}

StartQuery.prototype = Object.create(AbstractQuery.prototype);

StartQuery.prototype.close = function() {
}

StartQuery.prototype.nextSolution = function() {
    if (this.solution) {
        if (this.solution.length > 0) {
            return this.solution.shift();
        } else {
            delete this.solution;
        }
    }
}


// Helper functions

function createSolution(base) {
    var result = {};
    for(var attr in base) {
        if(base.hasOwnProperty(attr)) {
            result[attr] = base[attr];
        }
    }
    return result;
}


function compareTerms(t1, t2) {
    if(!t1) {
        return !t2 ? 0 : 1;
    }
    else if(!t2) {
        return -1;
    }
    var bt = t1.termType.localeCompare(t2.termType);
    if(bt != 0) {
        return bt;
    }
    else {
        // TODO: Does not handle numeric or date comparison
        var bv = t1.value.localeCompare(t2.value);
        if(bv != 0) {
            return bv;
        }
        else {
            if(t1.isLiteral()) {
                var bd = t1.datatype.uri.localeCompare(t2.datatype.uri);
                if(bd != 0) {
                    return bd;
                }
                else if(T("rdf:langString").equals(t1.datatype)) {
                    return t1.language.localeCompare(t2.language);
                }
                else {
                    return 0;
                }
            }
            else {
                return 0;
            }
        }
    }
}

function getLocalName(uri) {
    // TODO: This is not the 100% correct local name algorithm
    var index = uri.lastIndexOf("#");
    if(index < 0) {
        index = uri.lastIndexOf("/");
    }
    if(index < 0) {
        throw "Cannot get local name of " + uri;
    }
    return uri.substring(index + 1);
}


// class NodeSet
// (a super-primitive implementation for now!)

function NodeSet() {
    this.values = [];
}

NodeSet.prototype.add = function(node) {
    if(!this.contains(node)) {
        this.values.push(node);
    }
}

NodeSet.prototype.addAll = function(nodes) {
    for(var i = 0; i < nodes.length; i++) {
        this.add(nodes[i]);
    }
}

NodeSet.prototype.contains = function(node) {
    for(var i = 0; i < this.values.length; i++) {
        if(this.values[i].equals(node)) {
            return true;
        }
    }
    return false;
}

NodeSet.prototype.forEach = function(callback) {
    for(var i = 0; i < this.values.length; i++) {
        callback(this.values[i]);
    }
}

NodeSet.prototype.size = function() {
    return this.values.length;
}

NodeSet.prototype.toArray = function() {
    return this.values;
}

NodeSet.prototype.toString = function() {
    var str = "NodeSet(" + this.size() + "): [";
    var arr = this.toArray();
    for(var i = 0; i < arr.length; i++) {
        if(i > 0) {
            str += ", ";
        }
        str += arr[i];
    }
    return str + "]";
}


function var2Attr(varName) {
    if(!varName.indexOf("?") == 0) {
        throw "Variable name must start with ?";
    }
    if(varName.length == 1) {
        throw "Variable name too short";
    }
    return varName.substring(1);
}



// Simple Path syntax implementation:
// Adds all matches for a given subject and path combination into a given NodeSet.
// This should really be doing lazy evaluation and only up to the point
// where the match object is found.
function addPathValues(graph, subject, path, set) {
    if(path.uri) {
        set.addAll(RDFQuery(graph).match(subject, path, "?object").getNodeArray("?object"));
    }
    else if(Array.isArray(path)) {
        var s = new NodeSet();
        s.add(subject);
        for(var i = 0; i < path.length; i++) {
            var a = s.toArray();
            s = new NodeSet();
            for(var j = 0; j < a.length; j++) {
                addPathValues(graph, a[j], path[i], s);
            }
        }
        set.addAll(s.toArray());
    }
    else if(path.or) {
        for(var i = 0; i < path.or.length; i++) {
            addPathValues(graph, subject, path.or[i], set);
        }
    }
    else if(path.inverse) {
        if(path.inverse.isURI()) {
            set.addAll(RDFQuery(graph).match("?subject", path.inverse, subject).getNodeArray("?subject"));
        }
        else {
            throw "Unsupported: Inverse paths only work for named nodes";
        }
    }
    else if(path.zeroOrOne) {
        addPathValues(graph, subject, path.zeroOrOne, set);
        set.add(subject);
    }
    else if(path.zeroOrMore) {
        walkPath(graph, subject, path.zeroOrMore, set, new NodeSet());
        set.add(subject);
    }
    else if(path.oneOrMore) {
        walkPath(graph, subject, path.oneOrMore, set, new NodeSet());
    }
    else {
        throw "Unsupported path object: " + path;
    }
}

function walkPath(graph, subject, path, set, visited) {
    visited.add(subject);
    var s = new NodeSet();
    addPathValues(graph, subject, path, s);
    var a = s.toArray();
    set.addAll(a);
    for(var i = 0; i < a.length; i++) {
        if(!visited.contains(a[i])) {
            walkPath(graph, a[i], path, set, visited);
        }
    }
}