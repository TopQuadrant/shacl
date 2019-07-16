// http://datashapes.org/js/tests-rdfquery/paths.test#paths.js

function inversePathSingle() {
	return RDFQuery($data).
		path("sh:Shape", { inverse : T("rdfs:subClassOf") }, "?subClass").
		getCount();
}

function oneOrMore() {
	return RDFQuery($data).
		path(TermFactory.namedNode("http://datashapes.org/js/tests-rdfquery/paths.test#MergedClass"), { oneOrMore : T("rdfs:subClassOf") }, "?superClass").
		getCount();
}

function orPathLabelOrComment() {
	return RDFQuery($data).
		path("sh:Shape", { or : [ T("rdfs:label"), T("rdfs:comment") ] }, "?text").
		getCount();
}

function predicatePathSingle() {
	return RDFQuery($data).
		path("sh:Shape", "rdfs:label", "?label").
		getNode("?label");
}

function predicatePathChain() {
	return RDFQuery($data).
		path("sh:PropertyShape", "rdfs:subClassOf", "?Shape").
		path("?Shape", "rdfs:label", "?label").
		getNode("?label");
}

function sequencePath2() {
	return RDFQuery($data).
		path("owl:Thing", [ T("rdf:type"), T("rdfs:subClassOf") ], "?Class").
		getNode("?Class");
}

function sequencePath3() {
	return RDFQuery($data).
		path("owl:Thing", [ T("rdf:type"), T("rdfs:subClassOf"), T("rdf:type") ], "?type").
		getNode("?type");
}

function zeroOrOne() {
	return RDFQuery($data).
		path("owl:Thing", { zeroOrOne : T("rdf:type") }, "?type").
		getCount();
}

function zeroOrMore() {
	return RDFQuery($data).
		path(TermFactory.namedNode("http://datashapes.org/js/tests-rdfquery/paths.test#MergedClass"), { zeroOrMore : T("rdfs:subClassOf") }, "?superClass").
		getCount();
}
