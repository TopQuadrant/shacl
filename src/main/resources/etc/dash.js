// Functions implementing the validators of SHACL-JS

// There is no validator for sh:property as this is expected to be
// natively implemented by the surrounding engine.

function hasClass($value, $class) {
	return new RDFQueryUtil($dataGraph).isInstanceOf($value, $class);
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

function hasMaxExclusive($value, $maxExclusive) {
	return Number($value.value) < Number($maxExclusive.value);
}

function hasMaxInclusive($value, $maxInclusive) {
	return Number($value.value) <= Number($maxInclusive.value);
}

function hasMaxLength($value, $maxLength) {
	if($value.termType === "BlankNode") {
		return false;
	}
	return $value.value.length <= $maxLength.value;
}

function hasMinExclusive($value, $minExclusive) {
	return Number($value.value) > Number($minExclusive.value);
}

function hasMinInclusive($value, $minInclusive) {
	return Number($value.value) >= Number($minInclusive.value);
}

function hasMinLength($value, $minLength) {
	if($value.termType === "BlankNode") {
		return false;
	}
	return $value.value.length >= $minLength.value;
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

function hasNode($value, $node) {
	return SHACL.validateNode($value, $node).length == 0;
}

function hasNot($value, $not) {
	return SHACL.validateNode($value, $not).length > 0;
}

function hasPattern($value, $pattern, $flags) {
	if($value.termType === "BlankNode") {
		return false;
	}
	var re = $flags ? new RegExp($pattern.value, $flags.value) : new RegExp($pattern.value);
	return re.test($value.value);
}

function isAnd($value, $and) {
	var shapes = new RDFQueryUtil($shapesGraph).rdfListToArray($and);
	for(var i = 0; i < shapes.length; i++) {
		if(SHACL.validateNode($value, shapes[i]).length > 0) {
			return false;
		}
	}
	return true;
}

function isIn($value, $in) {
	var set = new NodeSet();
	set.addAll(new RDFQueryUtil($shapesGraph).rdfListToArray($in));
	return set.contains($value);
}

function isOr($value, $or) {
	var shapes = new RDFQueryUtil($shapesGraph).rdfListToArray($or);
	for(var i = 0; i < shapes.length; i++) {
		if(SHACL.validateNode($value, shapes[i]).length == 0) {
			return true;
		}
	}
	return false;
}

function isXor($value, $xor) {
	var shapes = new RDFQueryUtil($shapesGraph).rdfListToArray($xor);
	var count = 0;
	for(var i = 0; i < shapes.length; i++) {
		if(SHACL.validateNode($value, shapes[i]).length == 0) {
			count++;
		}
	}
	return count == 1;
}

function nodeHasValue($focusNode, $hasValue) {
	return $focusNode.equals($hasValue);
}

function nodeProperty($focusNode, $property) {
	return SHACL.validateNode($focusNode, $property);
}

function propertyDisjoint($focusNode, $path, $disjoint) {
	var results = [];
	RDFQuery($dataGraph).
		path($focusNode, toRDFQueryPath($path), "value").
		find($focusNode, $disjoint, "value").
		forEach(function(solution) {
					results.push({
						value: solution.value
					});
				});
	return results;
}

function propertyEquals($focusNode, $path, $equals) {
	var results = [];
	var path = toRDFQueryPath($path);
	RDFQuery($dataGraph).path($focusNode, path, "value").forEach(
		function(solution) {
			if(!RDFQuery($dataGraph).find($focusNode, $equals, solution.value).hasSolution()) {
				results.push({
					value: solution.value
				});
			}
		});
	RDFQuery($dataGraph).find($focusNode, $equals, "value").forEach(
		function(solution) {
			if(!RDFQuery($dataGraph).path($focusNode, path, solution.value).hasSolution()) {
				results.push({
					value: solution.value
				});
			}
		});
	return results;
}

function propertyHasValue($focusNode, $path, $hasValue) {
	var count = RDFQuery($dataGraph).path($focusNode, toRDFQueryPath($path), $hasValue).toArray().length;
	return count > 0;
}

function propertyLessThan($focusNode, $path, $lessThan) {
	var results = [];
	RDFQuery($dataGraph).
		path($focusNode, toRDFQueryPath($path), "value").
		find($focusNode, $lessThan, "otherValue").
		forEach(function(solution) {
					var c = compareTerms(solution.value, solution.otherValue);
					if(c >= 0) {
						results.push({
							value: solution.value
						});
					}
				});
	return results;
}

function propertyLessThanOrEquals($focusNode, $path, $lessThanOrEquals) {
	var results = [];
	RDFQuery($dataGraph).
		path($focusNode, toRDFQueryPath($path), "value").
		find($focusNode, $lessThanOrEquals, "otherValue").
		forEach(function(solution) {
					var c = compareTerms(solution.value, solution.otherValue);
					if(c > 0) {
						results.push({
							value: solution.value
						});
					}
				});
	return results;
}

function propertyMaxCount($focusNode, $path, $maxCount) {
	var count = RDFQuery($dataGraph).path($focusNode, toRDFQueryPath($path), "any").toArray().length;
	return count <= $maxCount.value;
}

function propertyMinCount($focusNode, $path, $minCount) {
	var count = RDFQuery($dataGraph).path($focusNode, toRDFQueryPath($path), "any").toArray().length;
	return count >= $minCount.value;
}

function propertyQualifiedMaxCount($focusNode, $path, $qualifiedValueShape, $qualifiedMaxCount, $currentShape) {
	var c = propertyQualifiedHelper($focusNode, $path, $qualifiedValueShape, $currentShape);
	return c <= $qualifiedMaxCount.value;
}

function propertyQualifiedMinCount($focusNode, $path, $qualifiedValueShape, $qualifiedMinCount, $currentShape) {
	var c = propertyQualifiedHelper($focusNode, $path, $qualifiedValueShape, $currentShape);
	return c >= $qualifiedMinCount.value;
}

function propertyQualifiedHelper($focusNode, $path, $qualifiedValueShape, $currentShape) {
	var siblingShapes = new NodeSet();
	RDFQuery($shapesGraph).
		find("parentShape", SH.property, $currentShape).
		find("parentShape", SH.qualifiedValueShapesDisjoint, XSDTrue).
		forEach(function(sol) {
				siblingShapes.addAll(RDFQuery($shapesGraph).
					find(sol.parentShape, SH.property, "sibling").
					find("sibling", SH.qualifiedValueShape, "qvs").
					filter(function(sol) { return !sol.qvs.equals($qualifiedValueShape) }).
					toNodeArray("qvs"));
			});
	return RDFQuery($dataGraph).
		path($focusNode, toRDFQueryPath($path), "value").
		filter(function(sol) { 
			return SHACL.validateNode(sol.value, $qualifiedValueShape).length == 0 &&
				!propertyQualifiedConformsToASibling(sol.value, siblingShapes.toArray()); 
		}).
		toArray().length;
}

function propertyQualifiedConformsToASibling(value, siblingShapes) {
	for(var i = 0; i < siblingShapes.length; i++) {
		if(SHACL.validateNode(value, siblingShapes[i]).length == 0) {
			return true;
		}
	}
	return false;
}

function propertyUniqueLang($focusNode, $uniqueLang, $path) {
	if(!XSD.boolean.equals($uniqueLang.datatype) || !("true" === $uniqueLang.value)) {
		return;
	}
	var it = RDFQuery($dataGraph).path($focusNode, toRDFQueryPath($path), "value");
	var map = {};
	for(var s = it.nextSolution(); s; s = it.nextSolution()) {
		var lang = s.value.language;
		if(lang && lang != "") {
			var old = map[lang];
			if(!old) {
				map[lang] = 1;
			}
			else {
				map[lang] = old + 1;
			}
		}
	}
	var results = [];
	for(var lang in map) {
		if(map.hasOwnProperty(lang)) {
			var count = map[lang];
			if(count > 1) {
				results.push("Language \"" + lang + "\" has been used by " + count + " values");
			}
		}
	}
	return results;
}

function validateClosed($value, $closed, $ignoredProperties, $currentShape) {
	if(!XSD.boolean.equals($closed.datatype) || !("true" === $closed.value)) {
		return;
	}
	var allowed = RDFQuery($shapesGraph).
		find($currentShape, SH.property, "propertyShape").
		find("propertyShape", SH.path, "path").
		filter(function(solution) { return solution.path.termType === "NamedNode" } ).
		toNodeSet("path");
	if($ignoredProperties) {
		allowed.addAll(new RDFQueryUtil($shapesGraph).rdfListToArray($ignoredProperties));
	}
	var results = [];
	RDFQuery($dataGraph).
		find($value, "predicate", "object").
		filter(function(solution) { return !allowed.contains(solution.predicate)}).
		forEach(function(solution) { 
			results.push({ 
				path : solution.predicate,
				value : solution.object
			});
		});
	return results;
}


function toRDFQueryPath(shPath) {
	// TODO: implement conforming to AbstractQuery.path syntax
	return shPath;
}


// Private helper functions

function RDFQueryUtil($source) {
	this.source = $source;
}

RDFQueryUtil.prototype.getInstancesOf = function($class) {
	var set = new NodeSet();
	var classes = this.getSubClassesOf($class);
	classes.add($class);
	var car = classes.toArray();
	for(var i = 0; i < car.length; i++) {
		set.addAll($RDFQuery(this.source).find("instance", RDF.type, car[i]).toNodeArray("instance"));
	}
	return set;
}

RDFQueryUtil.prototype.getObject = function($subject, $predicate) {
	return RDFQuery(this.source).find($subject, $predicate, "object").first().object;
}

RDFQueryUtil.prototype.getSubClassesOf = function($class) {
	var set = new NodeSet();
	this.walkSubjects(set, $class, RDFS.subClassOf);
	return set;
}

RDFQueryUtil.prototype.isInstanceOf = function($instance, $class) {
	var classes = this.getSubClassesOf($class);
	var types = RDFQuery($dataGraph).find($instance, RDF.type, "type");
	for(var n = types.nextSolution(); n; n = types.nextSolution()) {
		if(n.type.equals($class) || classes.contains(n.type)) {
			types.close();
			return true;
		}
	}
	return false;
}

RDFQueryUtil.prototype.rdfListToArray = function($rdfList) {
	var array = [];
	while(!RDF.nil.equals($rdfList)) {
		array.push(this.getObject($rdfList, RDF.first));
		$rdfList = this.getObject($rdfList, RDF.rest);
	}
	return array;
}

RDFQueryUtil.prototype.walkObjects = function($results, $subject, $predicate) {
	var it = this.source.find($subject, $predicate, null);
	for(var n = it.next(); n; n = it.next()) {
		if(!$results.contains(n.object)) {
			$results.add(n.object);
			this.walkObjects($results, n.object, $predicate);
		}
	}
}

RDFQueryUtil.prototype.walkSubjects = function($results, $object, $predicate) {
	var it = this.source.find(null, $predicate, $object);
	for(var n = it.next(); n; n = it.next()) {
		if(!$results.contains(n.subject)) {
			$results.add(n.subject);
			this.walkSubjects($results, n.subject, $predicate);
		}
	}
}
