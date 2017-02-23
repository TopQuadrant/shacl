// Functions implementing the validators of SHACL-JS
// Also include validators for the constraint components of the DASH namespace

// There is no validator for sh:property as this is expected to be
// natively implemented by the surrounding engine.

var XSDIntegerTypes = new NodeSet();
XSDIntegerTypes.add(T("xsd:integer"));

var XSDDecimalTypes = new NodeSet();
XSDDecimalTypes.addAll(XSDIntegerTypes.toArray());
XSDDecimalTypes.add(T("xsd:decimal"));
XSDDecimalTypes.add(T("xsd:float"));

function validateAnd($value, $and) {
	var shapes = new RDFQueryUtil($shapes).rdfListToArray($and);
	for(var i = 0; i < shapes.length; i++) {
		if(!SHACL.nodeConformsToShape($value, shapes[i])) {
			return false;
		}
	}
	return true;
}

function validateClass($value, $class) {
	return new RDFQueryUtil($data).isInstanceOf($value, $class);
}

function validateClosed($value, $closed, $ignoredProperties, $currentShape) {
	if(!T("true").equals($closed)) {
		return;
	}
	var allowed = $shapes.query().
		match($currentShape, "sh:property", "?propertyShape").
		match("?propertyShape", "sh:path", "?path").
		filter(function(solution) { return solution.path.isURI() } ).
		toNodeSet("?path");
	if($ignoredProperties) {
		allowed.addAll(new RDFQueryUtil($shapes).rdfListToArray($ignoredProperties));
	}
	var results = [];
	$data.query().
		match($value, "?predicate", "?object").
		filter(function(sol) { return !allowed.contains(sol.predicate)}).
		forEach(function(sol) { 
			results.push({ 
				path : sol.predicate,
				value : sol.object
			});
		});
	return results;
}

function validateClosedByTypesNode($focusNode, $closedByTypes) {
	if(!T("true").equals($closedByTypes)) {
		return;
	}
	var results = [];
	var allowedProperties = new NodeSet();
	$data.query().
		match($focusNode, "rdf:type", "?directType").
		path("?directType", { zeroOrMore : T("rdfs:subClassOf") }, "?type").
		forEachNode("?type", function(type) {
			$shapes.query().
				match(type, "sh:property", "?pshape").
				match("?pshape", "sh:path", "?path").
				filter(function(sol) { return sol.path.isURI() }).
				addAllNodes("?path", allowedProperties);
		});
	$data.query().
		match($focusNode, "?predicate", "?object").
		filter(function(sol) { return !T("rdf:type").equals(sol.predicate) }).
		filter(function(sol) { return !allowedProperties.contains(sol.predicate) }).
		forEach(function(sol) {
			results.push({
				path: sol.predicate,
				value: sol.object
			});
		})
	return results;
}

function validateDatatype($value, $datatype) {
	if($value.isLiteral()) {
		return $datatype.equals($value.datatype) && isValidForDatatype($value.lex, $datatype);
	}
	else {
		return false;
	}
}

function validateDisjointProperty($focusNode, $path, $disjoint) {
	var results = [];
	$data.query().
		path($focusNode, toRDFQueryPath($path), "?value").
		match($focusNode, $disjoint, "?value").
		forEach(function(solution) {
					results.push({
						value: solution.value
					});
				});
	return results;
}

function validateEqualsProperty($focusNode, $path, $equals) {
	var results = [];
	var path = toRDFQueryPath($path);
	$data.query().path($focusNode, path, "?value").forEach(
		function(solution) {
			if(!$data.query().match($focusNode, $equals, solution.value).hasSolution()) {
				results.push({
					value: solution.value
				});
			}
		});
	$data.query().match($focusNode, $equals, "?value").forEach(
		function(solution) {
			if(!$data.query().path($focusNode, path, solution.value).hasSolution()) {
				results.push({
					value: solution.value
				});
			}
		});
	return results;
}

function validateHasValueNode($focusNode, $hasValue) {
	return $focusNode.equals($hasValue);
}

function validateHasValueProperty($focusNode, $path, $hasValue) {
	var count = $data.query().path($focusNode, toRDFQueryPath($path), $hasValue).toArray().length;
	return count > 0;
}

function validateIn($value, $in) {
	var set = new NodeSet();
	set.addAll(new RDFQueryUtil($shapes).rdfListToArray($in));
	return set.contains($value);
}

function validateLanguageIn($value, $languageIn) {
	if(!$value.isLiteral()) {
		return false;
	}
	var lang = $value.language;
	if(!lang || lang === "") {
		return false;
	}
	var ls = new RDFQueryUtil($shapes).rdfListToArray($languageIn);
	for(var i = 0; i < ls.length; i++) {
		if(lang.startsWith(ls[i].lex)) {
			return true;
		}
	}
	return false;
}

function validateLessThanProperty($focusNode, $path, $lessThan) {
	var results = [];
	$data.query().
		path($focusNode, toRDFQueryPath($path), "?value").
		match($focusNode, $lessThan, "?otherValue").
		forEach(function(sol) {
					var c = compareTerms(sol.value, sol.otherValue);
					if(c >= 0) {
						results.push({
							value: sol.value
						});
					}
				});
	return results;
}

function validateLessThanOrEqualsProperty($focusNode, $path, $lessThanOrEquals) {
	var results = [];
	$data.query().
		path($focusNode, toRDFQueryPath($path), "?value").
		match($focusNode, $lessThanOrEquals, "?otherValue").
		forEach(function(sol) {
					var c = compareTerms(sol.value, sol.otherValue);
					if(c > 0) {
						results.push({
							value: sol.value
						});
					}
				});
	return results;
}

function validateMaxCountProperty($focusNode, $path, $maxCount) {
	var count = $data.query().path($focusNode, toRDFQueryPath($path), "?any").toArray().length;
	return count <= Number($maxCount.value);
}

function validateMaxExclusive($value, $maxExclusive) {
	return $value.isLiteral() && Number($value.lex) < Number($maxExclusive.lex);
}

function validateMaxInclusive($value, $maxInclusive) {
	return $value.isLiteral() && Number($value.lex) <= Number($maxInclusive.lex);
}

function validateMaxLength($value, $maxLength) {
	if($value.isBlankNode()) {
		return false;
	}
	return $value.value.length <= Number($maxLength.lex);
}

function validateMinCountProperty($focusNode, $path, $minCount) {
	var count = $data.query().path($focusNode, toRDFQueryPath($path), "?any").toArray().length;
	return count >= Number($minCount.value);
}

function validateMinExclusive($value, $minExclusive) {
	return $value.isLiteral() && Number($value.lex) > Number($minExclusive.lex);
}

function validateMinInclusive($value, $minInclusive) {
	return $value.isLiteral() && Number($value.lex) >= Number($minInclusive.lex);
}

function validateMinLength($value, $minLength) {
	if($value.isBlankNode()) {
		return false;
	}
	return $value.value.length >= Number($minLength.lex);
}

function validateNodeKind($value, $nodeKind) {
	if($value.isBlankNode()) {
		return T("sh:BlankNode").equals($nodeKind) || 
			T("sh:BlankNodeOrIRI").equals($nodeKind) ||
			T("sh:BlankNodeOrLiteral").equals($nodeKind);
	}
	else if($value.isURI()) {
		return T("sh:IRI").equals($nodeKind) || 
			T("sh:BlankNodeOrIRI").equals($nodeKind) ||
			T("sh:IRIOrLiteral").equals($nodeKind);
	}
	else if($value.isLiteral()) {
		return T("sh:Literal").equals($nodeKind) || 
			T("sh:BlankNodeOrLiteral").equals($nodeKind) ||
			T("sh:IRIOrLiteral").equals($nodeKind);
	}
}

function validateNode($value, $node) {
	return SHACL.nodeConformsToShape($value, $node);
}

function validateNonRecursiveProperty($focusNode, $path, $nonRecursive) {
	if(T("true").equals($nonRecursive)) {
		if($data.query().path($focusNode, toRDFQueryPath($path), $focusNode).hasSolution()) {
			return {
				path: $path,
				value: $focusNode
			}
		}
	}
}

function validateNot($value, $not) {
	return !SHACL.nodeConformsToShape($value, $not);
}

function validateOr($value, $or) {
	var shapes = new RDFQueryUtil($shapes).rdfListToArray($or);
	for(var i = 0; i < shapes.length; i++) {
		if(SHACL.nodeConformsToShape($value, shapes[i])) {
			return true;
		}
	}
	return false;
}

function validatePattern($value, $pattern, $flags) {
	if($value.isBlankNode()) {
		return false;
	}
	var re = $flags ? new RegExp($pattern.lex, $flags.lex) : new RegExp($pattern.lex);
	return re.test($value.value);
}

function validatePrimaryKeyProperty($focusNode, $path, $uriStart) {
	if(!$focusNode.isURI()) {
		return "Must be an IRI";
	}
	if($data.query().path($focusNode, toRDFQueryPath($path), null).toArray().length != 1) {
		return "Must have exactly one value";
	}
	var value = $data.query().path($focusNode, toRDFQueryPath($path), "?value").get("?value");
	var uri = $uriStart.lex + encodeURIComponent(value.value);
	if(!$focusNode.uri.equals(uri)) {
		return "Does not have URI " + uri;
	}
}

function validateQualifiedMaxCountProperty($focusNode, $path, $qualifiedValueShape, $qualifiedMaxCount, $currentShape) {
	var c = validateQualifiedHelper($focusNode, $path, $qualifiedValueShape, $currentShape);
	return c <= Number($qualifiedMaxCount.lex);
}

function validateQualifiedMinCountProperty($focusNode, $path, $qualifiedValueShape, $qualifiedMinCount, $currentShape) {
	var c = validateQualifiedHelper($focusNode, $path, $qualifiedValueShape, $currentShape);
	return c >= Number($qualifiedMinCount.lex);
}

function validateQualifiedHelper($focusNode, $path, $qualifiedValueShape, $currentShape) {
	var siblingShapes = new NodeSet();
	$shapes.query().
		match("?parentShape", "sh:property", $currentShape).
		match("?parentShape", "sh:qualifiedValueShapesDisjoint", "true").
		forEach(function(sol) {
				siblingShapes.addAll($shapes.query().
					match(sol.parentShape, "sh:property", "?sibling").
					match("?sibling", "sh:qualifiedValueShape", "?qvs").
					filter(function(sol) { return !sol.qvs.equals($qualifiedValueShape) }).
					toNodeArray("?qvs"));
			});
	return $data.query().
		path($focusNode, toRDFQueryPath($path), "?value").
		filter(function(sol) { 
			return SHACL.nodeConformsToShape(sol.value, $qualifiedValueShape) &&
				!validateQualifiedConformsToASibling(sol.value, siblingShapes.toArray()); 
		}).
		toArray().length;
}

function validateQualifiedConformsToASibling(value, siblingShapes) {
	for(var i = 0; i < siblingShapes.length; i++) {
		if(SHACL.nodeConformsToShape(value, siblingShapes[i])) {
			return true;
		}
	}
	return false;
}

function validateRootClass($value, $rootClass) {
	return $data.query().path($value, { zeroOrMore: T("rdfs:subClassOf") }, $rootClass).hasSolution();
}

function validateStem($value, $stem) {
	return $value.isURI() && $value.uri.startsWith($stem.lex);
}

function validateSubSetOf($focusNode, $subSetOf, $value) {
	return $data.query().match($focusNode, $subSetOf, $value).hasSolution();
}

function validateUniqueLangProperty($focusNode, $uniqueLang, $path) {
	if(!T("true").equals($uniqueLang)) {
		return;
	}
	var map = {};
	$data.query().path($focusNode, toRDFQueryPath($path), "?value").forEach(function(sol) {
		var lang = sol.value.language;
		if(lang && lang != "") {
			var old = map[lang];
			if(!old) {
				map[lang] = 1;
			}
			else {
				map[lang] = old + 1;
			}
		}
	})
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

function validateXone($value, $xone) {
	var shapes = new RDFQueryUtil($shapes).rdfListToArray($xone);
	var count = 0;
	for(var i = 0; i < shapes.length; i++) {
		if(SHACL.nodeConformsToShape($value, shapes[i])) {
			count++;
		}
	}
	return count == 1;
}


function toRDFQueryPath(shPath) {
	if(shPath.isURI()) {
		return shPath;
	}
	else if(shPath.isBlankNode()) {
		var util = new RDFQueryUtil($shapes);
		if(util.getObject(shPath, T("rdf:first"))) {
			var paths = util.rdfListToArray(shPath);
			var result = [];
			for(var i = 0; i < paths.length; i++) {
				result.push(toRDFQueryPath(paths[i]));
			}
			return result;
		}
		var alternativePath = util.getObject(shPath, T("sh:alternativePath"));
		if(alternativePath) {
			var paths = util.rdfListToArray(alternativePath);
			var result = [];
			for(var i = 0; i < paths.length; i++) {
				result.push(toRDFQueryPath(paths[i]));
			}
			return { or : result };
		}
		var zeroOrMorePath = util.getObject(shPath, T("sh:zeroOrMorePath"));
		if(zeroOrMorePath) {
			return { zeroOrMore : toRDFQueryPath(zeroOrMorePath) };
		}
		var oneOrMorePath = util.getObject(shPath, T("sh:oneOrMorePath"));
		if(oneOrMorePath) {
			return { oneOrMore : toRDFQueryPath(oneOrMorePath) };
		}
		var zeroOrOnePath = util.getObject(shPath, T("sh:zeroOrOnePath"));
		if(zeroOrOnePath) {
			return { zeroOrOne : toRDFQueryPath(zeroOrOnePath) };
		}
		var inversePath = util.getObject(shPath, T("sh:inversePath"));
		if(inversePath) {
			return { inverse : toRDFQueryPath(inversePath) };
		}
	}
	throw "Unsupported SHACL path " + shPath;
	// TODO: implement conforming to AbstractQuery.path syntax
	return shPath;
}


// Private helper functions

//TODO: Support more datatypes
function isValidForDatatype(lex, datatype) {
	if(XSDIntegerTypes.contains(datatype)) {
		var r = parseInt(lex);
		return !isNaN(r);
	}
	else if(XSDDecimalTypes.contains(datatype)) {
		var r = parseFloat(lex);
		return !isNan(r);
	}
	else {
		return true;
	}
}

function RDFQueryUtil($source) {
	this.source = $source;
}

RDFQueryUtil.prototype.getInstancesOf = function($class) {
	var set = new NodeSet();
	var classes = this.getSubClassesOf($class);
	classes.add($class);
	var car = classes.toArray();
	for(var i = 0; i < car.length; i++) {
		set.addAll(RDFQuery(this.source).match("?instance", "rdf:type", car[i]).toNodeArray("?instance"));
	}
	return set;
}

RDFQueryUtil.prototype.getObject = function($subject, $predicate) {
	if(!$subject) {
		throw "Missing subject";
	}
	if(!$predicate) {
		throw "Missing predicate";
	}
	return RDFQuery(this.source).match($subject, $predicate, "?object").get("?object");
}

RDFQueryUtil.prototype.getSubClassesOf = function($class) {
	var set = new NodeSet();
	this.walkSubjects(set, $class, T("rdfs:subClassOf"));
	return set;
}

RDFQueryUtil.prototype.isInstanceOf = function($instance, $class) {
	var classes = this.getSubClassesOf($class);
	var types = $data.query().match($instance, "rdf:type", "?type");
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
	while(!T("rdf:nil").equals($rdfList)) {
		array.push(this.getObject($rdfList, T("rdf:first")));
		$rdfList = this.getObject($rdfList, T("rdf:rest"));
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
