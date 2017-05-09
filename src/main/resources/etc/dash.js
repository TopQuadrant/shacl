// Functions implementing the validators of SHACL-JS
// Also include validators for the constraint components of the DASH namespace

// Also included: implementations of the standard DASH functions

// There is no validator for sh:property as this is expected to be
// natively implemented by the surrounding engine.

var XSDIntegerTypes = new NodeSet();
XSDIntegerTypes.add(T("xsd:integer"));

var XSDDecimalTypes = new NodeSet();
XSDDecimalTypes.addAll(XSDIntegerTypes.toArray());
XSDDecimalTypes.add(T("xsd:decimal"));
XSDDecimalTypes.add(T("xsd:float"));

validateAnd = function ($value, $and) {
    var shapes = new RDFQueryUtil($shapes).rdfListToArray($and);
    for (var i = 0; i < shapes.length; i++) {
        if (!SHACL.nodeConformsToShape($value, shapes[i])) {
            return false;
        }
    }
    return true;
}

validateClass = function ($value, $class) {
    return new RDFQueryUtil($data).isInstanceOf($value, $class);
}

validateClosed = function ($value, $closed, $ignoredProperties, $currentShape) {
    if (!T("true").equals($closed)) {
        return;
    }
    var allowed = $shapes.query().
        match($currentShape, "sh:property", "?propertyShape").
        match("?propertyShape", "sh:path", "?path").
        filter(function (solution) { return solution.path.isURI() }).
        getNodeSet("?path");
    if ($ignoredProperties) {
        allowed.addAll(new RDFQueryUtil($shapes).rdfListToArray($ignoredProperties));
    }
    var results = [];
    $data.query().
        match($value, "?predicate", "?object").
        filter(function (sol) { return !allowed.contains(sol.predicate) }).
        forEach(function (sol) {
            results.push({
                path: sol.predicate,
                value: sol.object
            });
        });
    return results;
}

validateClosedByTypesNode = function ($this, $closedByTypes) {
    if (!T("true").equals($closedByTypes)) {
        return;
    }
    var results = [];
    var allowedProperties = new NodeSet();
    $data.query().
        match($this, "rdf:type", "?directType").
        path("?directType", { zeroOrMore: T("rdfs:subClassOf") }, "?type").
        forEachNode("?type", function (type) {
            $shapes.query().
                match(type, "sh:property", "?pshape").
                match("?pshape", "sh:path", "?path").
                filter(function (sol) { return sol.path.isURI() }).
                addAllNodes("?path", allowedProperties);
        });
    $data.query().
        match($this, "?predicate", "?object").
        filter(function (sol) { return !T("rdf:type").equals(sol.predicate) }).
        filter(function (sol) { return !allowedProperties.contains(sol.predicate) }).
        forEach(function (sol) {
            results.push({
                path: sol.predicate,
                value: sol.object
            });
        })
    return results;
}

validateDatatype = function ($value, $datatype) {
    if ($value.isLiteral()) {
        return $datatype.equals($value.datatype) && isValidForDatatype($value.lex, $datatype);
    }
    else {
        return false;
    }
}

validateDisjoint = function ($this, $value, $disjoint) {
    return !$data.query().match($this, $disjoint, $value).hasSolution();
}

validateEqualsProperty = function ($this, $path, $equals) {
    var results = [];
    var path = toRDFQueryPath($path);
    $data.query().path($this, path, "?value").forEach(
        function (solution) {
            if (!$data.query().match($this, $equals, solution.value).hasSolution()) {
                results.push({
                    value: solution.value
                });
            }
        });
    $data.query().match($this, $equals, "?value").forEach(
        function (solution) {
            if (!$data.query().path($this, path, solution.value).hasSolution()) {
                results.push({
                    value: solution.value
                });
            }
        });
    return results;
}

validateHasValueNode = function ($this, $hasValue) {
    return $this.equals($hasValue);
}

validateHasValueProperty = function ($this, $path, $hasValue) {
    var count = $data.query().path($this, toRDFQueryPath($path), $hasValue).getCount();
    return count > 0;
}

validateIn = function ($value, $in) {
    var set = new NodeSet();
    set.addAll(new RDFQueryUtil($shapes).rdfListToArray($in));
    return set.contains($value);
}

validateLanguageIn = function ($value, $languageIn) {
    if (!$value.isLiteral()) {
        return false;
    }
    var lang = $value.language;
    if (!lang || lang === "") {
        return false;
    }
    var ls = new RDFQueryUtil($shapes).rdfListToArray($languageIn);
    for (var i = 0; i < ls.length; i++) {
        if (lang.startsWith(ls[i].lex)) {
            return true;
        }
    }
    return false;
}

validateLessThanProperty = function ($this, $path, $lessThan) {
    var results = [];
    $data.query().
        path($this, toRDFQueryPath($path), "?value").
        match($this, $lessThan, "?otherValue").
        forEach(function (sol) {
            var c = SHACL.compareNodes(sol.value, sol.otherValue);
            if (c == null || c >= 0) {
                results.push({
                    value: sol.value
                });
            }
        });
    return results;
}

validateLessThanOrEqualsProperty = function ($this, $path, $lessThanOrEquals) {
    var results = [];
    $data.query().
        path($this, toRDFQueryPath($path), "?value").
        match($this, $lessThanOrEquals, "?otherValue").
        forEach(function (sol) {
            var c = SHACL.compareNodes(sol.value, sol.otherValue);
            if (c == null || c > 0) {
                results.push({
                    value: sol.value
                });
            }
        });
    return results;
}

validateMaxCountProperty = function ($this, $path, $maxCount) {
    var count = $data.query().path($this, toRDFQueryPath($path), "?any").getCount();
    return count <= Number($maxCount.value);
}

validateMaxExclusive = function ($value, $maxExclusive) {
    return $value.isLiteral() && Number($value.lex) < Number($maxExclusive.lex);
}

validateMaxInclusive = function ($value, $maxInclusive) {
    return $value.isLiteral() && Number($value.lex) <= Number($maxInclusive.lex);
}

validateMaxLength = function ($value, $maxLength) {
    if ($value.isBlankNode()) {
        return false;
    }
    return $value.value.length <= Number($maxLength.lex);
}

validateMinCountProperty = function ($this, $path, $minCount) {
    var count = $data.query().path($this, toRDFQueryPath($path), "?any").getCount();
    return count >= Number($minCount.value);
}

validateMinExclusive = function ($value, $minExclusive) {
    return $value.isLiteral() && Number($value.lex) > Number($minExclusive.lex);
}

validateMinInclusive = function ($value, $minInclusive) {
    return $value.isLiteral() && Number($value.lex) >= Number($minInclusive.lex);
}

validateMinLength = function ($value, $minLength) {
    if ($value.isBlankNode()) {
        return false;
    }
    return $value.value.length >= Number($minLength.lex);
}

validateNodeKind = function ($value, $nodeKind) {
    if ($value.isBlankNode()) {
        return T("sh:BlankNode").equals($nodeKind) ||
            T("sh:BlankNodeOrIRI").equals($nodeKind) ||
            T("sh:BlankNodeOrLiteral").equals($nodeKind);
    }
    else if ($value.isURI()) {
        return T("sh:IRI").equals($nodeKind) ||
            T("sh:BlankNodeOrIRI").equals($nodeKind) ||
            T("sh:IRIOrLiteral").equals($nodeKind);
    }
    else if ($value.isLiteral()) {
        return T("sh:Literal").equals($nodeKind) ||
            T("sh:BlankNodeOrLiteral").equals($nodeKind) ||
            T("sh:IRIOrLiteral").equals($nodeKind);
    }
}

validateNode = function ($value, $node) {
    return SHACL.nodeConformsToShape($value, $node);
}

validateNonRecursiveProperty = function ($this, $path, $nonRecursive) {
    if (T("true").equals($nonRecursive)) {
        if ($data.query().path($this, toRDFQueryPath($path), $this).hasSolution()) {
            return {
                path: $path,
                value: $this
            }
        }
    }
}

validateNot = function ($value, $not) {
    return !SHACL.nodeConformsToShape($value, $not);
}

validateOr = function ($value, $or) {
    var shapes = new RDFQueryUtil($shapes).rdfListToArray($or);
    for (var i = 0; i < shapes.length; i++) {
        if (SHACL.nodeConformsToShape($value, shapes[i])) {
            return true;
        }
    }
    return false;
}

validatePattern = function ($value, $pattern, $flags) {
    if ($value.isBlankNode()) {
        return false;
    }
    var re = $flags ? new RegExp($pattern.lex, $flags.lex) : new RegExp($pattern.lex);
    return re.test($value.value);
}

validatePrimaryKeyProperty = function ($this, $path, $uriStart) {
    if (!$this.isURI()) {
        return "Must be an IRI";
    }
    if ($data.query().path($this, toRDFQueryPath($path), null).getCount() != 1) {
        return "Must have exactly one value";
    }
    var value = $data.query().path($this, toRDFQueryPath($path), "?value").getNode("?value");
    var uri = $uriStart.lex + encodeURIComponent(value.value);
    if (!$this.uri.equals(uri)) {
        return "Does not have URI " + uri;
    }
}

validateQualifiedMaxCountProperty = function ($this, $path, $qualifiedValueShape, $qualifiedValueShapesDisjoint, $qualifiedMaxCount, $currentShape) {
    var c = validateQualifiedHelper($this, $path, $qualifiedValueShape, $qualifiedValueShapesDisjoint, $currentShape);
    return c <= Number($qualifiedMaxCount.lex);
}

validateQualifiedMinCountProperty = function ($this, $path, $qualifiedValueShape, $qualifiedValueShapesDisjoint, $qualifiedMinCount, $currentShape) {
    var c = validateQualifiedHelper($this, $path, $qualifiedValueShape, $qualifiedValueShapesDisjoint, $currentShape);
    return c >= Number($qualifiedMinCount.lex);
}

validateQualifiedHelper = function ($this, $path, $qualifiedValueShape, $qualifiedValueShapesDisjoint, $currentShape) {
    var siblingShapes = new NodeSet();
    if (T("true").equals($qualifiedValueShapesDisjoint)) {
        $shapes.query().
            match("?parentShape", "sh:property", $currentShape).
            match("?parentShape", "sh:property", "?sibling").
            match("?sibling", "sh:qualifiedValueShape", "?siblingShape").
            filter(exprNotEquals("?siblingShape", $qualifiedValueShape)).
            addAllNodes("?siblingShape", siblingShapes);
    }
    return $data.query().
        path($this, toRDFQueryPath($path), "?value").
        filter(function (sol) {
            return SHACL.nodeConformsToShape(sol.value, $qualifiedValueShape) &&
                !validateQualifiedConformsToASibling(sol.value, siblingShapes.toArray());
        }).
        getCount();
}

validateQualifiedConformsToASibling = function (value, siblingShapes) {
    for (var i = 0; i < siblingShapes.length; i++) {
        if (SHACL.nodeConformsToShape(value, siblingShapes[i])) {
            return true;
        }
    }
    return false;
}

validateRootClass = function ($value, $rootClass) {
    return $data.query().path($value, { zeroOrMore: T("rdfs:subClassOf") }, $rootClass).hasSolution();
}

validateStem = function ($value, $stem) {
    return $value.isURI() && $value.uri.startsWith($stem.lex);
}

validateSubSetOf = function ($this, $subSetOf, $value) {
    return $data.query().match($this, $subSetOf, $value).hasSolution();
}

validateUniqueLangProperty = function ($this, $uniqueLang, $path) {
    if (!T("true").equals($uniqueLang)) {
        return;
    }
    var map = {};
    $data.query().path($this, toRDFQueryPath($path), "?value").forEach(function (sol) {
        var lang = sol.value.language;
        if (lang && lang != "") {
            var old = map[lang];
            if (!old) {
                map[lang] = 1;
            }
            else {
                map[lang] = old + 1;
            }
        }
    });
    var results = [];
    for (var lang in map) {
        if (map.hasOwnProperty(lang)) {
            var count = map[lang];
            if (count > 1) {
                results.push("Language \"" + lang + "\" has been used by " + count + " values");
            }
        }
    }
    return results;
}

validateXone = function ($value, $xone) {
    var shapes = new RDFQueryUtil($shapes).rdfListToArray($xone);
    var count = 0;
    for (var i = 0; i < shapes.length; i++) {
        if (SHACL.nodeConformsToShape($value, shapes[i])) {
            count++;
        }
    }
    return count == 1;
}


toRDFQueryPath = function (shPath) {
    if (shPath.isURI()) {
        return shPath;
    }
    else if (shPath.isBlankNode()) {
        var util = new RDFQueryUtil($shapes);
        if ($shapes.query().getObject(shPath, "rdf:first")) {
            var paths = util.rdfListToArray(shPath);
            var result = [];
            for (var i = 0; i < paths.length; i++) {
                result.push(toRDFQueryPath(paths[i]));
            }
            return result;
        }
        var alternativePath = $shapes.query().getObject(shPath, "sh:alternativePath");
        if (alternativePath) {
            var paths = util.rdfListToArray(alternativePath);
            var result = [];
            for (var i = 0; i < paths.length; i++) {
                result.push(toRDFQueryPath(paths[i]));
            }
            return { or: result };
        }
        var zeroOrMorePath = $shapes.query().getObject(shPath, "sh:zeroOrMorePath");
        if (zeroOrMorePath) {
            return { zeroOrMore: toRDFQueryPath(zeroOrMorePath) };
        }
        var oneOrMorePath = $shapes.query().getObject(shPath, "sh:oneOrMorePath");
        if (oneOrMorePath) {
            return { oneOrMore: toRDFQueryPath(oneOrMorePath) };
        }
        var zeroOrOnePath = $shapes.query().getObject(shPath, "sh:zeroOrOnePath");
        if (zeroOrOnePath) {
            return { zeroOrOne: toRDFQueryPath(zeroOrOnePath) };
        }
        var inversePath = $shapes.query().getObject(shPath, "sh:inversePath");
        if (inversePath) {
            return { inverse: toRDFQueryPath(inversePath) };
        }
    }
    throw "Unsupported SHACL path " + shPath;
    // TODO: implement conforming to AbstractQuery.path syntax
    return shPath;
}


// Private helper functions

//TODO: Support more datatypes
isValidForDatatype = function (lex, datatype) {
    if (XSDIntegerTypes.contains(datatype)) {
        var r = parseInt(lex);
        return !isNaN(r);
    }
    else if (XSDDecimalTypes.contains(datatype)) {
        var r = parseFloat(lex);
        return !isNan(r);
    }
    else {
        return true;
    }
}

RDFQueryUtil = function ($source) {
    this.source = $source;
}

RDFQueryUtil.prototype.getInstancesOf = function ($class) {
    var set = new NodeSet();
    var classes = this.getSubClassesOf($class);
    classes.add($class);
    var car = classes.toArray();
    for (var i = 0; i < car.length; i++) {
        set.addAll(RDFQuery(this.source).match("?instance", "rdf:type", car[i]).getNodeArray("?instance"));
    }
    return set;
}

RDFQueryUtil.prototype.getObject = function ($subject, $predicate) {
    if (!$subject) {
        throw "Missing subject";
    }
    if (!$predicate) {
        throw "Missing predicate";
    }
    return RDFQuery(this.source).match($subject, $predicate, "?object").getNode("?object");
}

RDFQueryUtil.prototype.getSubClassesOf = function ($class) {
    var set = new NodeSet();
    this.walkSubjects(set, $class, T("rdfs:subClassOf"));
    return set;
}

RDFQueryUtil.prototype.isInstanceOf = function ($instance, $class) {
    var classes = this.getSubClassesOf($class);
    var types = $data.query().match($instance, "rdf:type", "?type");
    for (var n = types.nextSolution(); n; n = types.nextSolution()) {
        if (n.type.equals($class) || classes.contains(n.type)) {
            types.close();
            return true;
        }
    }
    return false;
}

RDFQueryUtil.prototype.rdfListToArray = function ($rdfList) {
    if ($rdfList.elements != null) {
        return $rdfList.elements;
    } else {
        var array = [];
        while (!T("rdf:nil").equals($rdfList)) {
            array.push(this.getObject($rdfList, T("rdf:first")));
            $rdfList = this.getObject($rdfList, T("rdf:rest"));
        }
        return array;
    }
}

RDFQueryUtil.prototype.walkObjects = function ($results, $subject, $predicate) {
    var it = this.source.find($subject, $predicate, null);
    for (var n = it.next(); n; n = it.next()) {
        if (!$results.contains(n.object)) {
            $results.add(n.object);
            this.walkObjects($results, n.object, $predicate);
        }
    }
}

RDFQueryUtil.prototype.walkSubjects = function ($results, $object, $predicate) {
    var it = this.source.find(null, $predicate, $object);
    for (var n = it.next(); n; n = it.next()) {
        if (!$results.contains(n.subject)) {
            $results.add(n.subject);
            this.walkSubjects($results, n.subject, $predicate);
        }
    }
}
