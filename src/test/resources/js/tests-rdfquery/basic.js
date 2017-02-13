function bindNewLabel() {
	return RDFQuery($dataGraph).
		find(OWL.Class, RDFS.label, "label").
		bind("newLabel", function(solution) { return TermFactory.literal(solution.label.value, "en-US") }).
		first().newLabel;
}

function countAllSubClasses() {
	var count = RDFQuery($dataGraph).
		find("subClass", RDFS.subClassOf, NS.sh("Shape")).
		toArray().length;
	return TermFactory.literal(count, XSD.decimal);
}

function getFirstType() {
	return RDFQuery($dataGraph).
		find(NS.sh("Shape"), RDF.type, "type").
		first().type;
}

function joinTwoBGPsThenFilter() {
	return RDFQuery($dataGraph).
		find(OWL.Class, RDFS.label, "label").
		find("otherClass", RDFS.label, "label").
		filter(function(solution) { return !OWL.Class.equals(solution.otherClass) }).
		first().otherClass;
}

function limit10() {
	return RDFQuery($dataGraph).
		find(null, RDFS.label, null).
		limit(10).toArray().length;
}

function orderByVarProperty() {
	var list = RDFQuery($dataGraph).
		find("property", RDFS.domain, RDF.Statement).
		orderByVar("property").
		toNodeArray("property");
	assert(list.length === 3, "Unexpected length " + list.length);
	assert(RDF.object.equals(list[0]), "First item should be rdf:object but was " + list[0]);
	assert(RDF.predicate.equals(list[1]), "Second item should be rdf:predicate but was " + list[1]);
	assert(RDF.subject.equals(list[2]), "Third item should be rdf:subject but was " + list[2]);
	return true;
}

// Verify that the start query produces a single binding with zero attributes
function startEmpty() {
	var query = RDFQuery($dataGraph);
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