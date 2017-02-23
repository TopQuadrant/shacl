function bindNewLabel() {
	return RDFQuery($data).
		match("owl:Class", "rdfs:label", "?label").
		bind("?newLabel", function(sol) { return TermFactory.literal(sol.label.value, "en-US") }).
		first().newLabel;
}

function countAllSubClasses() {
	var count = RDFQuery($data).
		match("?subClass", "rdfs:subClassOf", "sh:Shape").
		toArray().length;
	return TermFactory.literal(count, T("xsd:decimal"));
}

function getFirstType() {
	return RDFQuery($data).
		match("sh:Shape", "rdf:type", "?type").
		first().type;
}

function joinTwoBGPsThenFilter() {
	return RDFQuery($data).
		match("owl:Class", "rdfs:label", "?label").
		match("?otherClass", "rdfs:label", "?label").
		filter(function(sol) { return !T("owl:Class").equals(sol.otherClass) }).
		first().otherClass;
}

function limit10() {
	return RDFQuery($data).
		match(null, "rdfs:label", null).
		limit(10).toArray().length;
}

function orderByVarProperty() {
	var list = RDFQuery($data).
		match("?property", "rdfs:domain", "rdf:Statement").
		orderBy("?property").
		toNodeArray("?property");
	assert(list.length === 3, "Unexpected length " + list.length);
	assert(T("rdf:object").equals(list[0]), "First item should be rdf:object but was " + list[0]);
	assert(T("rdf:predicate").equals(list[1]), "Second item should be rdf:predicate but was " + list[1]);
	assert(T("rdf:subject").equals(list[2]), "Third item should be rdf:subject but was " + list[2]);
	return true;
}

// Verify that the start query produces a single binding with zero attributes
function startEmpty() {
	var query = RDFQuery($data);
	var first = query.nextSolution();
	var count = 0;
	for (var k in first) {
	    if (first.hasOwnProperty(k)) {
	       ++count;
	    }
	}
	if(count != 0) {
		return "Expected empty object";
	}
	var next = query.nextSolution();
	if(next != null) {
		return "Expected null";
	}
	return "OK";
}


function assert(condition, message) {
    if (!condition) {
        throw message || "Assertion failed";
    }
}