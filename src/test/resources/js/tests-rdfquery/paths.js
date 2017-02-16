// http://datashapes.org/js/tests-rdfquery/paths.test#paths.js

function inversePathSingle() {
	return RDFQuery($dataGraph).
		path(SH.Shape, { inverse : RDFS.subClassOf }, "subClass").
		toArray().length;
}

function oneOrMore() {
	return RDFQuery($dataGraph).
		path(TermFactory.namedNode("http://datashapes.org/js/tests-rdfquery/paths.test#MergedClass"), { oneOrMore : RDFS.subClassOf }, "superClass").
		toArray().length;
}

function orPathLabelOrComment() {
	return RDFQuery($dataGraph).
		path(SH.Shape, { or : [ RDFS.label, RDFS.comment ] }, "text").
		toArray().length;
}

function predicatePathSingle() {
	return RDFQuery($dataGraph).
		path(SH.Shape, RDFS.label, "label").
		first().label;
}

function predicatePathChain() {
	return RDFQuery($dataGraph).
		path(SH.PropertyShape, RDFS.subClassOf, "Shape").
		path("Shape", RDFS.label, "label").
		first().label;
}

function sequencePath2() {
	return RDFQuery($dataGraph).
		path(OWL.Thing, [ RDF.type, RDFS.subClassOf ], "Class").
		first().Class;
}

function sequencePath3() {
	return RDFQuery($dataGraph).
		path(OWL.Thing, [RDF.type, RDFS.subClassOf, RDF.type], "type").
		first().type;
}

function zeroOrOne() {
	return RDFQuery($dataGraph).
		path(OWL.Thing, { zeroOrOne : RDF.type }, "type").
		toArray().length;
}

function zeroOrMore() {
	return RDFQuery($dataGraph).
		path(TermFactory.namedNode("http://datashapes.org/js/tests-rdfquery/paths.test#MergedClass"), { zeroOrMore : RDFS.subClassOf }, "superClass").
		toArray().length;
}
