// rdfquery.js
// A simple RDF query library for JavaScript
// Contact: holger@topquadrant.com
//
// The basic idea is that the function RDFQuery produces an initial
// Query object, which starts with a single "empty" solution.
// Each query object has a function nextSolution() producing an iteration
// of variable bindings ("volcano style").
// Each query object can be refined with subsequent calls to other
// functions, producing new queries.
// Invoking nextSolution on a query will pull solutions from its
// predecessors in a chain of query objects.
// Finally, functions such as .first() and .toArray() can be used
// to produce individual values.
// The solution objects are plain JavaScript objects providing a
// mapping from variable names to RDF Term objects.
// Unless a query has been walked to exhaustion, .close() must be called.
//
// RDF Term objects are expected to follow the contracts from the
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

/* Example:
	var result = RDFQuery($dataGraph).
		find(OWL.Class, RDFS.label, "label").
		find("otherClass", RDFS.label, "label").
		filter(function(solution) { return !OWL.Class.equals(solution.otherClass) }).
		first().otherClass;
		
Equivalent SPARQL:
		SELECT ?otherClass
		WHERE {
			owl:Class rdfs:label ?label .
			?otherClass rdfs:label ?label .
			FILTER (owl:Class != ?otherClass) .
		} LIMIT 1
*/


function _Namespace (nsuri) {
	return function (localName) {
		return TermFactory.namedNode(nsuri + localName);
	}
}

// Suggested container object for all frequently needed namespaces
// Usage to get rdf:type: NS.rdf("type")
var NS = {
	rdf : _Namespace('http://www.w3.org/1999/02/22-rdf-syntax-ns#'),
	rdfs : _Namespace('http://www.w3.org/2000/01/rdf-schema#'),
	sh : _Namespace('http://www.w3.org/ns/shacl#'),
	owl : _Namespace('http://www.w3.org/2002/07/owl#'),
	xsd : _Namespace('http://www.w3.org/2001/XMLSchema#')
}

var OWL = {
	Class : NS.owl("Class"),
	DatatypeProperty : NS.owl("DatatypeProperty"),
	ObjectProperty : NS.owl("ObjectProperty"),
	Thing : NS.owl("Thing")
}

var RDF = {
	HTML : NS.rdf("HTML"),
	List : NS.rdf("List"),
	Property : NS.rdf("Property"),
	Statement : NS.rdf("Statement"),
	first : NS.rdf("first"),
	langString : NS.rdf("langString"),
	nil : NS.rdf("nil"),
	object : NS.rdf("object"),
	predicate : NS.rdf("predicate"),
	rest : NS.rdf("rest"),
	subject : NS.rdf("subject"),
	type : NS.rdf("type"),
	value : NS.rdf("value")
}

var RDFS = {
	Class : NS.rdfs("Class"),
	Datatype : NS.rdfs("Datatype"),
	Literal : NS.rdfs("Literal"),
	Resource : NS.rdfs("Resource"),
	comment : NS.rdfs("comment"),
	domain : NS.rdfs("domain"),
	label : NS.rdfs("label"),
	range : NS.rdfs("range"),
	seeAlso : NS.rdfs("seeAlso"),
	subClassOf : NS.rdfs("subClassOf"),
	subPropertyOf : NS.rdfs("subPropertyOf")
}

var SH = {
	ConstraintComponent : NS.sh("ConstraintComponent"),	
	JSValidator : NS.sh("JSValidator"),
	NodeShape : NS.sh("NodeShape"),
	PropertyConstraintComponent : NS.sh("PropertyConstraintComponent"),	
	PropertyShape : NS.sh("PropertyShape"),
	Shape : NS.sh("Shape"),
	alternativePath : NS.sh("alternativePath"),
	inversePath : NS.sh("inversePath"),
	jsFunctionName : NS.sh("jsFunctionName"),
	qualifiedValueShape : NS.sh("qualifiedValueShape"),
	qualifiedValueShapesDisjoint : NS.sh("qualifiedValueShapesDisjoint"),
	nodeValidator : NS.sh("nodeValidator"),
	oneOrMorePath : NS.sh("oneOrMorePath"),
	optional : NS.sh("optional"),
	parameter : NS.sh("parameter"),
	path : NS.sh("path"),
	property : NS.sh("property"),
	propertyValidator : NS.sh("propertyValidator"),
	target : NS.sh("target"),
	targetClass : NS.sh("targetClass"),
	targetNode : NS.sh("targetNode"),
	targetObjectsOf : NS.sh("targetObjectsOf"),
	targetSubjectsOf : NS.sh("targetSubjectsOf"),
	validator : NS.sh("validator"),
	zeroOrMorePath : NS.sh("zeroOrMorePath"),
	zeroOrOnePath : NS.sh("zeroOrOnePath")
}

var XSD = {
	boolean : NS.xsd("boolean"),
	date : NS.xsd("date"),
	dateTime : NS.xsd("dateTime"),
	decimal : NS.xsd("decimal"),
	float : NS.xsd("float"),
	integer : NS.xsd("integer"),
	string : NS.xsd("string")
};

var XSDTrue = TermFactory.literal("true", XSD.boolean);
var XSDFalse = TermFactory.literal("false", XSD.boolean);


/**
 * Creates a query object for a given graph and optional initial solution.
 * The resulting object can be further refined using the functions on
 * AbstractQuery such as <code>find()</code> and <code>filter()</code>.
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

    // Query constructor functions...

/**
 * Creates a new query that adds a binding for a given variable into
 * each solution produced by the input query.
 * @param varName  the name of the variable to bind
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
 * Creates a new query doing a triple match.
 * @param s  the match subject or null (any) or a variable name (string)
 * @param p  the match predicate or null (any) or a variable name (string)
 * @param o  the match object or null (any) or a variable name (string)
 */
AbstractQuery.prototype.find = function(s, p, o) {
	return new RDFTripleQuery(this, s, p, o);
}

/**
 * Creates a new query that only allows the first n solutions through.
 * @param limit  the maximum number of results to allow
 */
AbstractQuery.prototype.limit = function(limit) {
	return new LimitQuery(this, limit);
}

/**
 * Creates a new query that sorts all input solutions by the bindings
 * for a given variable.
 * @param varName  the name of the variable to sort by
 */
AbstractQuery.prototype.orderByVar = function(varName) {
	return new OrderByVarQuery(this, varName);
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
	if(path && path.termType === "NamedNode") {
		return new RDFTripleQuery(this, s, path, o);
	}
	else {
		return new PathQuery(this, s, path, o);
	}
}

// TODO: add other SPARQL-like query types
//       - .distinct()
//       - .path(s, path, o)   (this is complex)
//       - .union(otherQuery)


	// Terminal functions - exhaust the solution iterators

/**
 * Gets the next solution and closes the query.
 * @return a solution object
 */
AbstractQuery.prototype.first = function() {
	var n = this.nextSolution();
	this.close();
	return n;
}

AbstractQuery.prototype.forEach = function(callback) {
	for(var n = this.nextSolution(); n; n = this.nextSolution()) {
		callback(n);
	}
}

/**
 * Gets the next solution and, if that exists, returns the binding for a
 * given variable from that solution.
 * @param varName  the name of the binding to get
 * @return the value of the variable or null or undefined if it doesn't exist
 */
AbstractQuery.prototype.get = function(varName) {
	var s = this.first();
	if(s) {
		return s[varName];
	}
	else {
		return null;
	}
}

AbstractQuery.prototype.hasSolution = function() {
	return this.first() != null;
}

/**
 * Turns all results into an array.
 * @return an array consisting of solution objects
 */
AbstractQuery.prototype.toArray = function() {
	var results = [];
	for(var n = this.nextSolution(); n != null; n = this.nextSolution()) {
		results.push(n);
	}
	return results;
}

/**
 * Turns all results into an array of bindings for a given variable.
 * @return an array consisting of RDF node objects
 */
AbstractQuery.prototype.toNodeArray = function(varName) {
	var results = [];
	for(var n = this.nextSolution(); n != null; n = this.nextSolution()) {
		results.push(n[varName]);
	}
	return results;
}

/**
 * Turns all results into a set of bindings for a given variable.
 * The set has functions .contains and .toArray. 
 * @return a set consisting of RDF node objects
 */
AbstractQuery.prototype.toNodeSet = function(varName) {
	var results = new NodeSet();
	for(var n = this.nextSolution(); n != null; n = this.nextSolution()) {
		results.add(n[varName]);
	}
	return results;
}


// END OF PUBLIC API ------------------------


// class BindQuery
// Takes all input solutions but adds a value for a given variable so that
// the value is computed by a given function based on the current solution.
// It is illegal to use a variable that already has a value from the input.

function BindQuery(input, varName, bindFunction) {
	this.source = input.source;
	this.input = input;
	this.bindFunction = bindFunction;
	this.varName = varName;
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
			result[this.varName] = newNode;
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


// class OrderByVarQuery
// Sorts all solutions from the input stream by a given variable

function OrderByVarQuery(input, varName) {
	this.input = input;
	this.source = input.source;
	this.varName = varName;
}

OrderByVarQuery.prototype = Object.create(AbstractQuery.prototype);

OrderByVarQuery.prototype.close = function() {
	this.input.close();
}

OrderByVarQuery.prototype.nextSolution = function() {
	if(!this.solutions) {
		this.solutions = this.input.toArray();
		var varName = this.varName;
		this.solutions.sort(function(s1, s2) {
				return compareTerms(s1[varName], s2[varName]);
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
	this.subject = subject;
	if(path == null) {
		throw "Path cannot be unbound";
	}
	this.path = path;
	this.object = object;
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
		if(typeof this.object === 'string') {
			result[this.object] = n;
		}
		if(this.pathIndex == r.length) {
			delete this.pathResults; // Mark as exhausted
		}
		return result;
	}
	
	// Pull from input
	this.inputSolution = this.input.nextSolution();
	if(this.inputSolution) {
		var sm = (typeof this.subject === 'string') ? this.inputSolution[this.subject] : this.subject;
		if(sm == null) {
			throw "Path cannot have unbound subject";
		}
		var om = (typeof this.object === 'string') ? this.inputSolution[this.object] : this.object;
		var pathResultsSet = new NodeSet();
		addPathValues(this.source, sm, this.path, pathResultsSet);
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


// class RDFTripleQuery
// Joins the solutions from the input Query with triple matches against
// the current input graph.

function RDFTripleQuery(input, s, p, o) {
	this.source = input.source;
	this.input = input;
	this.s = s;
	this.p = p;
	this.o = o;
}

RDFTripleQuery.prototype = Object.create(AbstractQuery.prototype);

RDFTripleQuery.prototype.close = function() {
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
RDFTripleQuery.prototype.nextSolution = function() {

	var oit = this.ownIterator;
	if(oit) {
		var n = oit.next();
		if(n != null) {
			var result = createSolution(this.inputSolution);
			if(typeof this.s === 'string') {
				result[this.s] = n.subject;
			}
			if(typeof this.p === 'string') {
				result[this.p] = n.predicate;
			}
			if(typeof this.o === 'string') {
				result[this.o] = n.object;
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
		var sm = (typeof this.s === 'string') ? this.inputSolution[this.s] : this.s;
		var pm = (typeof this.p === 'string') ? this.inputSolution[this.p] : this.p;
		var om = (typeof this.o === 'string') ? this.inputSolution[this.o] : this.o;
		this.ownIterator = this.source.find(sm, pm, om)
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
	this.solution = initialSolution;
}

StartQuery.prototype = Object.create(AbstractQuery.prototype);

StartQuery.prototype.close = function() {
}

StartQuery.prototype.nextSolution = function() {
	if(this.solution) {
		var b = this.solution;
		delete this.solution;
		return b;
	}
	else {
		return null;
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
			if(t1.termType === "Literal") {
				var bd = t1.datatype.value.localeCompare(t2.datatype.value);
				if(bd != 0) {
					return bd;
				}
				else if(RDF.langString.equals(t1.datatype)) {
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


// Simple Path syntax implementation:
// Adds all matches for a given subject and path combination into a given NodeSet.
// This should really be doing lazy evaluation and only up to the point
// where the match object is found.
function addPathValues(graph, subject, path, set) {
	if(path.termType === "NamedNode") {
		set.addAll(RDFQuery(graph).find(subject, path, "object").toNodeArray("object"));
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
		if(path.inverse.termType === "NamedNode") {
			set.addAll(RDFQuery(graph).find("subject", path.inverse, subject).toNodeArray("subject"));
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
