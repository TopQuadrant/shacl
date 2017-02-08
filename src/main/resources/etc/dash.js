// Functions implementing the validators of SHACL-JS
// Work in progress!

function hasClass($value, $class) {
	var types = RDFQuery($dataGraph).find($value, RDF.type, "type");
	for(var n = types.nextSolution(); n != null; n = types.nextSolution()) {
		// TODO: Also check for subclasses
		if(n.type.equals($class)) {
			types.close();
			return true;
		}
	}
	return false;
}

function hasDatatype($value, $datatype) {
	if($value.termType === "Literal") {
		// TODO: Check for ill-formed XSD literals
		return $datatype.equals($value.datatype);
	}
	else {
		return false;
	}
}

function hasNodeKind($value, $nodeKind) {
	if($value.termType === "BlankNode") {
		return NS.sh("BlankNode").equals($nodeKind) || 
			NS.sh("BlankNodeOrIRI").equals($nodeKind) ||
			NS.sh("BlankNodeOrLiteral").equals($nodeKind);
	}
	else if($value.termType === "NamedNode") {
		return NS.sh("IRI").equals($nodeKind) || 
			NS.sh("BlankNodeOrIRI").equals($nodeKind) ||
			NS.sh("IRIOrLiteral").equals($nodeKind);
	}
	else if($value.termType === "Literal") {
		return NS.sh("Literal").equals($nodeKind) || 
			NS.sh("BlankNodeOrLiteral").equals($nodeKind) ||
			NS.sh("IRIOrLiteral").equals($nodeKind);
	}
}

function hasNot($value, $not) {
	return SHACL.validateNode($value, $not, $dataGraph, $shapesGraph).length > 0;
}

function propertyMaxCount($focusNode, $path, $maxCount) {
	var it = $dataGraph.find($focusNode, $path, null);
	var count = 0;
	for(var t = it.next(); t != null; t = it.next()) {
		count++;
	}
	return count <= $maxCount.value;
}

function propertyMinCount($focusNode, $path, $minCount) {
	var it = $dataGraph.find($focusNode, $path, null);
	var count = 0;
	for(var t = it.next(); t != null; t = it.next()) {
		count++;
	}
	return count >= $minCount.value;
}


// Private helper functions

