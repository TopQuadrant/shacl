// Functions implementing the validators of SHACL-JS
// Also include validators for the constraint components of the DASH namespace

// There is no validator for sh:property as this is expected to be
// natively implemented by the surrounding engine.

var rdfquery = require("./rdfquery");
var T = rdfquery.T;
var validator = require("./shacl-validator");

var common = require("./common");
var $shapes = common.$shapes;
var $data = common.$data;

var XSDIntegerTypes = new rdfquery.NodeSet();
XSDIntegerTypes.add(T("xsd:integer"));

var XSDDecimalTypes = new rdfquery.NodeSet();
XSDDecimalTypes.addAll(XSDIntegerTypes.toArray());
XSDDecimalTypes.add(T("xsd:decimal"));
XSDDecimalTypes.add(T("xsd:float"));

var validateAnd = function ($value, $and) {
    var shapes = new rdfquery.RDFQueryUtil($shapes()).rdfListToArray($and);
    for (var i = 0; i < shapes.length; i++) {
        if (!validator.SHACL.nodeConformsToShape($value, shapes[i])) {
            return false;
        }
    }
    return true;
};

var validateClass = function ($value, $class) {
    return new rdfquery.RDFQueryUtil($data()).isInstanceOf($value, $class);
};

var validateClosed = function ($value, $closed, $ignoredProperties, $currentShape) {
    if (!T("true").equals($closed)) {
        return;
    }
    var allowed = $shapes().query().
        match($currentShape, "sh:property", "?propertyShape").
        match("?propertyShape", "sh:path", "?path").
        filter(function (solution) { return solution.path.isURI() }).
        getNodeSet("?path");
    if ($ignoredProperties) {
        allowed.addAll(new rdfquery.RDFQueryUtil($shapes()).rdfListToArray($ignoredProperties));
    }
    var results = [];
    $data().query().
        match($value, "?predicate", "?object").
        filter(function (sol) { return !allowed.contains(sol.predicate) }).
        forEach(function (sol) {
            results.push({
                path: sol.predicate,
                value: sol.object
            });
        });
    return results;
};

var validateClosedByTypesNode = function ($this, $closedByTypes) {
    if (!T("true").equals($closedByTypes)) {
        return;
    }
    var results = [];
    var allowedProperties = new rdfquery.NodeSet();
    $data().query().
        match($this, "rdf:type", "?directType").
        path("?directType", { zeroOrMore: T("rdfs:subClassOf") }, "?type").
        forEachNode("?type", function (type) {
            $shapes().query().
                match(type, "sh:property", "?pshape").
                match("?pshape", "sh:path", "?path").
                filter(function (sol) { return sol.path.isURI() }).
                addAllNodes("?path", allowedProperties);
        });
    $data().query().
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
};

var validateDatatype = function ($value, $datatype) {
    if ($value.isLiteral()) {
        return $datatype.equals($value.datatype) && isValidForDatatype($value.lex, $datatype);
    }
    else {
        return false;
    }
};

var validateDisjoint = function ($this, $value, $disjoint) {
    return !$data().query().match($this, $disjoint, $value).hasSolution();
};

var validateEqualsProperty = function ($this, $path, $equals) {
    var results = [];
    var path = rdfquery.toRDFQueryPath($path);
    $data().query().path($this, path, "?value").forEach(
        function (solution) {
            if (!$data().query().match($this, $equals, solution.value).hasSolution()) {
                results.push({
                    value: solution.value
                });
            }
        });
    $data().query().match($this, $equals, "?value").forEach(
        function (solution) {
            if (!$data().query().path($this, path, solution.value).hasSolution()) {
                results.push({
                    value: solution.value
                });
            }
        });
    return results;
};

var validateHasValueNode = function ($this, $hasValue) {
    return $this.equals($hasValue);
};

var validateHasValueProperty = function ($this, $path, $hasValue) {
    var count = $data().query().path($this, rdfquery.toRDFQueryPath($path), $hasValue).getCount();
    return count > 0;
};

var validateIn = function ($value, $in) {
    var set = new rdfquery.NodeSet();
    set.addAll(new rdfquery.RDFQueryUtil($shapes()).rdfListToArray($in));
    return set.contains($value);
}

var validateLanguageIn = function ($value, $languageIn) {
    if (!$value.isLiteral()) {
        return false;
    }
    var lang = $value.language;
    if (!lang || lang === "") {
        return false;
    }
    var ls = new rdfquery.RDFQueryUtil($shapes()).rdfListToArray($languageIn);
    for (var i = 0; i < ls.length; i++) {
        if (lang.startsWith(ls[i].lex)) {
            return true;
        }
    }
    return false;
}

var validateLessThanProperty = function ($this, $path, $lessThan) {
    var results = [];
    $data().query().
        path($this, rdfquery.toRDFQueryPath($path), "?value").
        match($this, $lessThan, "?otherValue").
        forEach(function (sol) {
            var c = validator.SHACL.compareNodes(sol.value, sol.otherValue);
            if (c == null || c >= 0) {
                results.push({
                    value: sol.value
                });
            }
        });
    return results;
}

var validateLessThanOrEqualsProperty = function ($this, $path, $lessThanOrEquals) {
    var results = [];
    $data().query().
        path($this, rdfquery.toRDFQueryPath($path), "?value").
        match($this, $lessThanOrEquals, "?otherValue").
        forEach(function (sol) {
            var c = validator.SHACL.compareNodes(sol.value, sol.otherValue);
            if (c == null || c > 0) {
                results.push({
                    value: sol.value
                });
            }
        });
    return results;
}

var validateMaxCountProperty = function ($this, $path, $maxCount) {
    var count = $data().query().path($this, rdfquery.toRDFQueryPath($path), "?any").getCount();
    return count <= Number($maxCount.value);
}

var validateMaxExclusive = function ($value, $maxExclusive) {
    return $value.isLiteral() && Number($value.lex) < Number($maxExclusive.lex);
}

var validateMaxInclusive = function ($value, $maxInclusive) {
    return $value.isLiteral() && Number($value.lex) <= Number($maxInclusive.lex);
}

var validateMaxLength = function ($value, $maxLength) {
    if ($value.isBlankNode()) {
        return false;
    }
    return $value.value.length <= Number($maxLength.lex);
}

var validateMinCountProperty = function ($this, $path, $minCount) {
    var count = $data().query().path($this, rdfquery.toRDFQueryPath($path), "?any").getCount();
    return count >= Number($minCount.value);
}

var validateMinExclusive = function ($value, $minExclusive) {
    return $value.isLiteral() && Number($value.lex) > Number($minExclusive.lex);
}

var validateMinInclusive = function ($value, $minInclusive) {
    return $value.isLiteral() && Number($value.lex) >= Number($minInclusive.lex);
}

var validateMinLength = function ($value, $minLength) {
    if ($value.isBlankNode()) {
        return false;
    }
    return $value.value.length >= Number($minLength.lex);
}

var validateNodeKind = function ($value, $nodeKind) {
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

var validateNode = function ($value, $node) {
    console.log("validateNode...");
    return validator.SHACL.nodeConformsToShape($value, $node);
}

var validateNonRecursiveProperty = function ($this, $path, $nonRecursive) {
    if (T("true").equals($nonRecursive)) {
        if ($data().query().path($this, rdfquery.toRDFQueryPath($path), $this).hasSolution()) {
            return {
                path: $path,
                value: $this
            }
        }
    }
}

var validateNot = function ($value, $not) {
    return !validator.SHACL.nodeConformsToShape($value, $not);
}

var validateOr = function ($value, $or) {
    var shapes = new rdfquery.RDFQueryUtil($shapes()).rdfListToArray($or);
    for (var i = 0; i < shapes.length; i++) {
        if (validator.SHACL.nodeConformsToShape($value, shapes[i])) {
            return true;
        }
    }
    return false;
}

var validatePattern = function ($value, $pattern, $flags) {
    if ($value.isBlankNode()) {
        return false;
    }
    var re = $flags ? new RegExp($pattern.lex, $flags.lex) : new RegExp($pattern.lex);
    return re.test($value.value);
}

var validatePrimaryKeyProperty = function ($this, $path, $uriStart) {
    if (!$this.isURI()) {
        return "Must be an IRI";
    }
    if ($data().query().path($this, rdfquery.toRDFQueryPath($path), null).getCount() != 1) {
        return "Must have exactly one value";
    }
    var value = $data().query().path($this, rdfquery.toRDFQueryPath($path), "?value").getNode("?value");
    var uri = $uriStart.lex + encodeURIComponent(value.value);
    if (!$this.uri.equals(uri)) {
        return "Does not have URI " + uri;
    }
}

var validateQualifiedMaxCountProperty = function ($this, $path, $qualifiedValueShape, $qualifiedValueShapesDisjoint, $qualifiedMaxCount, $currentShape) {
    var c = validateQualifiedHelper($this, $path, $qualifiedValueShape, $qualifiedValueShapesDisjoint, $currentShape);
    return c <= Number($qualifiedMaxCount.lex);
}

var validateQualifiedMinCountProperty = function ($this, $path, $qualifiedValueShape, $qualifiedValueShapesDisjoint, $qualifiedMinCount, $currentShape) {
    var c = validateQualifiedHelper($this, $path, $qualifiedValueShape, $qualifiedValueShapesDisjoint, $currentShape);
    return c >= Number($qualifiedMinCount.lex);
}

var validateQualifiedHelper = function ($this, $path, $qualifiedValueShape, $qualifiedValueShapesDisjoint, $currentShape) {
    var siblingShapes = new rdfquery.NodeSet();
    if (T("true").equals($qualifiedValueShapesDisjoint)) {
        $shapes().query().
            match("?parentShape", "sh:property", $currentShape).
            match("?parentShape", "sh:property", "?sibling").
            match("?sibling", "sh:qualifiedValueShape", "?siblingShape").
            filter(exprNotEquals("?siblingShape", $qualifiedValueShape)).
            addAllNodes("?siblingShape", siblingShapes);
    }
    return $data().query().
        path($this, rdfquery.toRDFQueryPath($path), "?value").
        filter(function (sol) {
            return validator.SHACL.nodeConformsToShape(sol.value, $qualifiedValueShape) &&
                !validateQualifiedConformsToASibling(sol.value, siblingShapes.toArray());
        }).
        getCount();
}

var validateQualifiedConformsToASibling = function (value, siblingShapes) {
    for (var i = 0; i < siblingShapes.length; i++) {
        if (validator.SHACL.nodeConformsToShape(value, siblingShapes[i])) {
            return true;
        }
    }
    return false;
}

var validateRootClass = function ($value, $rootClass) {
    return $data().query().path($value, { zeroOrMore: T("rdfs:subClassOf") }, $rootClass).hasSolution();
}

var validateStem = function ($value, $stem) {
    return $value.isURI() && $value.uri.startsWith($stem.lex);
}

var validateSubSetOf = function ($this, $subSetOf, $value) {
    return $data().query().match($this, $subSetOf, $value).hasSolution();
}

var validateUniqueLangProperty = function ($this, $uniqueLang, $path) {
    if (!T("true").equals($uniqueLang)) {
        return;
    }
    var map = {};
    $data().query().path($this, rdfquery.toRDFQueryPath($path), "?value").forEach(function (sol) {
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

var validateXone = function ($value, $xone) {
    var shapes = new rdfquery.RDFQueryUtil($shapes()).rdfListToArray($xone);
    var count = 0;
    for (var i = 0; i < shapes.length; i++) {
        if (validator.SHACL.nodeConformsToShape($value, shapes[i])) {
            count++;
        }
    }
    return count == 1;
}




// Private helper functions

//TODO: Support more datatypes
var isValidForDatatype = function (lex, datatype) {
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


validator.ValidationFunction.functionRegistry.validateAnd = validateAnd;
validator.ValidationFunction.functionRegistry.validateClass = validateClass;
validator.ValidationFunction.functionRegistry.validateClosed = validateClosed;
validator.ValidationFunction.functionRegistry.validateClosedByTypesNode = validateClosedByTypesNode;
validator.ValidationFunction.functionRegistry.validateDatatype = validateDatatype;
validator.ValidationFunction.functionRegistry.validateDisjoint = validateDisjoint;
validator.ValidationFunction.functionRegistry.validateEqualsProperty = validateEqualsProperty;
validator.ValidationFunction.functionRegistry.validateHasValueNode = validateHasValueNode;
validator.ValidationFunction.functionRegistry.validateHasValueProperty = validateHasValueProperty;
validator.ValidationFunction.functionRegistry.validateIn = validateIn;
validator.ValidationFunction.functionRegistry.validateLanguageIn = validateLanguageIn;
validator.ValidationFunction.functionRegistry.validateLessThanProperty = validateLessThanProperty;
validator.ValidationFunction.functionRegistry.validateLessThanOrEqualsProperty = validateLessThanOrEqualsProperty;
validator.ValidationFunction.functionRegistry.validateMaxCountProperty = validateMaxCountProperty;
validator.ValidationFunction.functionRegistry.validateMaxExclusive = validateMaxExclusive;
validator.ValidationFunction.functionRegistry.validateMaxInclusive = validateMaxInclusive;
validator.ValidationFunction.functionRegistry.validateMaxLength = validateMaxLength;
validator.ValidationFunction.functionRegistry.validateMinCountProperty = validateMinCountProperty;
validator.ValidationFunction.functionRegistry.validateMinExclusive = validateMinExclusive;
validator.ValidationFunction.functionRegistry.validateMinInclusive = validateMinInclusive;
validator.ValidationFunction.functionRegistry.validateMinLength = validateMinLength;
validator.ValidationFunction.functionRegistry.validateNodeKind = validateNodeKind;
validator.ValidationFunction.functionRegistry.validateNode = validateNode;
validator.ValidationFunction.functionRegistry.validateNonRecursiveProperty = validateNonRecursiveProperty;
validator.ValidationFunction.functionRegistry.validateNot = validateNot;
validator.ValidationFunction.functionRegistry.validateOr = validateOr;
validator.ValidationFunction.functionRegistry.validatePattern = validatePattern;
validator.ValidationFunction.functionRegistry.validatePrimaryKeyProperty = validatePrimaryKeyProperty;
validator.ValidationFunction.functionRegistry.validateQualifiedMaxCountProperty = validateQualifiedMaxCountProperty;
validator.ValidationFunction.functionRegistry.validateQualifiedMinCountProperty = validateQualifiedMinCountProperty;
validator.ValidationFunction.functionRegistry.validateQualifiedHelper = validateQualifiedHelper;
validator.ValidationFunction.functionRegistry.validateQualifiedConformsToASibling = validateQualifiedConformsToASibling;
validator.ValidationFunction.functionRegistry.validateRootClass = validateRootClass;
validator.ValidationFunction.functionRegistry.validateStem = validateStem;
validator.ValidationFunction.functionRegistry.validateSubSetOf = validateSubSetOf;
validator.ValidationFunction.functionRegistry.validateUniqueLangProperty = validateUniqueLangProperty;
validator.ValidationFunction.functionRegistry.validateXone = validateXone;
