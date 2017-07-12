// Functions implementing the validators of SHACL-JS
// Also include validators for the constraint components of the DASH namespace

// There is no validator for sh:property as this is expected to be
// natively implemented by the surrounding engine.

var rdfquery = require("./rdfquery");
var NodeSet = require("./rdfquery/node-set");
var ValidationEngine = require("./validation-engine");
var T = rdfquery.T;

var coerceValue = function(t) {
    if (t.datatype == null) {
        return t.value;
    } else if (t.datatype.uri === "http://www.w3.org/2001/XMLSchema#integer") {
        return parseInt(t.value);
    } else if (t.datatype.uri === "http://www.w3.org/2001/XMLSchema#float") {
        return parseFloat(t.value);
    } else {
        return t.value;
    }
};
var compareTerms = function (t1, t2) {
    if (!t1) {
        return !t2 ? 0 : 1;
    }
    else if (!t2) {
        return -1;
    }
    var bt = t1.termType.localeCompare(t2.termType);
    if (bt !== 0) {
        return bt;
    }
    else {
        // TODO: Does not handle numeric or date comparison
        var v1 = coerceValue(t1);
        var v2 = coerceValue(t2);
        if (v1 !== v2) {
            return (v1 <= v2) ? -1 : 1;
        }
        else {
            if (t1.isLiteral()) {
                var bd = t1.datatype.uri.localeCompare(t2.datatype.uri);
                if (bd !== 0) {
                    return bd;
                }
                else if (T("rdf:langString").equals(t1.datatype)) {
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
};

var registerDASH = function(context) {

    var depth = 0;

    var compareNodes = function (node1, node2) {
        // TODO: Does not handle the case where nodes cannot be compared
        return compareTerms(node1, node2);
    };

    var nodeConformsToShape = function (focusNode, shapeNode) {
        var localEngine = new ValidationEngine(context, true);
        var shape = context.shapesGraph.getShape(shapeNode);
        try {
            depth++;
            return !localEngine.validateNodeAgainstShape(focusNode, shape, context.$data);
        }
        finally {
            depth--;
        }
    };
    
    var XSDIntegerTypes = new NodeSet();
    XSDIntegerTypes.add(T("xsd:integer"));

    var XSDDecimalTypes = new NodeSet();
    XSDDecimalTypes.addAll(XSDIntegerTypes.toArray());
    XSDDecimalTypes.add(T("xsd:decimal"));
    XSDDecimalTypes.add(T("xsd:float"));

    var validateAnd = function ($value, $and) {
        var shapes = context.$shapes.query().rdfListToArray($and);
        for (var i = 0; i < shapes.length; i++) {
            if (!nodeConformsToShape($value, shapes[i])) {
                return false;
            }
        }
        return true;
    };

    var validateClass = function ($value, $class) {
        return rdfquery.isInstanceOf($value, $class, context);
    };

    var validateClosed = function ($value, $closed, $ignoredProperties, $currentShape) {
        if (!T("true").equals($closed)) {
            return;
        }
        var allowed = context.$shapes.query().match($currentShape, "sh:property", "?propertyShape").match("?propertyShape", "sh:path", "?path").filter(function (solution) {
            return solution.path.isURI()
        }).getNodeSet("?path");
        if ($ignoredProperties) {
            allowed.addAll(context.$shapes.query().rdfListToArray($ignoredProperties));
        }
        var results = [];
        context.$data.query().match($value, "?predicate", "?object").filter(function (sol) {
            return !allowed.contains(sol.predicate)
        }).forEach(function (sol) {
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
        var allowedProperties = new NodeSet();
        context.$data.query().match($this, "rdf:type", "?directType").path("?directType", {zeroOrMore: T("rdfs:subClassOf")}, "?type").forEachNode("?type", function (type) {
            context.$shapes.query().match(type, "sh:property", "?pshape").match("?pshape", "sh:path", "?path").filter(function (sol) {
                return sol.path.isURI()
            }).addAllNodes("?path", allowedProperties);
        });
        context.$data.query().match($this, "?predicate", "?object").filter(function (sol) {
            return !T("rdf:type").equals(sol.predicate)
        }).filter(function (sol) {
            return !allowedProperties.contains(sol.predicate)
        }).forEach(function (sol) {
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
        return !context.$data.query().match($this, $disjoint, $value).hasSolution();
    };

    var validateEqualsProperty = function ($this, $path, $equals) {
        var results = [];
        var path = rdfquery.toRDFQueryPath($path, context);
        context.$data.query().path($this, path, "?value").forEach(
            function (solution) {
                if (!context.$data.query().match($this, $equals, solution.value).hasSolution()) {
                    results.push({
                        value: solution.value
                    });
                }
            });
        context.$data.query().match($this, $equals, "?value").forEach(
            function (solution) {
                if (!context.$data.query().path($this, path, solution.value).hasSolution()) {
                    results.push({
                        value: solution.value
                    });
                }
            });
        return results;
    };

    var validateEqualsNode = function ($this, $equals) {
        var results = [];
        var solutions = 0;
        context.$data.query().path($this, $equals, "?value").forEach(
            function (solution) {
                solutions++;
                if (compareNodes($this, solution['value']) !== 0) {
                    results.push({
                        value: solution.value
                    });
                }
            });
        if (results.length === 0 && solutions === 0) {
            results.push({
                value: $this.value
            });
        }
        return results;
    };

    var validateHasValueNode = function ($this, $hasValue) {
        return $this.equals($hasValue);
    };

    var validateHasValueProperty = function ($this, $path, $hasValue) {
        var count = context.$data.query().path($this, rdfquery.toRDFQueryPath($path, context), $hasValue).getCount();
        return count > 0;
    };

    var validateIn = function ($value, $in) {
        var set = new NodeSet();
        set.addAll(context.$shapes.query().rdfListToArray($in));
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
        var ls = context.$shapes.query().rdfListToArray($languageIn);
        for (var i = 0; i < ls.length; i++) {
            if (lang.startsWith(ls[i].lex)) {
                return true;
            }
        }
        return false;
    }

    var validateLessThanProperty = function ($this, $path, $lessThan) {
        var results = [];
        context.$data.query().path($this, rdfquery.toRDFQueryPath($path, context), "?value").match($this, $lessThan, "?otherValue").forEach(function (sol) {
            var c = compareNodes(sol.value, sol.otherValue);
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
        context.$data.query().path($this, rdfquery.toRDFQueryPath($path, context), "?value").match($this, $lessThanOrEquals, "?otherValue").forEach(function (sol) {
            var c = compareNodes(sol.value, sol.otherValue);
            if (c == null || c > 0) {
                results.push({
                    value: sol.value
                });
            }
        });
        return results;
    }

    var validateMaxCountProperty = function ($this, $path, $maxCount) {
        var count = context.$data.query().path($this, rdfquery.toRDFQueryPath($path, context), "?any").getCount();
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
        var count = context.$data.query().path($this, rdfquery.toRDFQueryPath($path, context), "?any").getCount();
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
        return nodeConformsToShape($value, $node);
    }

    var validateNonRecursiveProperty = function ($this, $path, $nonRecursive) {
        if (T("true").equals($nonRecursive)) {
            if (context.$data.query().path($this, rdfquery.toRDFQueryPath($path, context), $this).hasSolution()) {
                return {
                    path: $path,
                    value: $this
                }
            }
        }
    }

    var validateNot = function ($value, $not) {
        return !nodeConformsToShape($value, $not);
    }

    var validateOr = function ($value, $or) {
        var shapes = context.$shapes.query().rdfListToArray($or);
        for (var i = 0; i < shapes.length; i++) {
            if (nodeConformsToShape($value, shapes[i])) {
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
        if (context.$data.query().path($this, rdfquery.toRDFQueryPath($path, context), null).getCount() != 1) {
            return "Must have exactly one value";
        }
        var value = context.$data.query().path($this, rdfquery.toRDFQueryPath($path), "?value").getNode("?value");
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
        var siblingShapes = new NodeSet();
        if (T("true").equals($qualifiedValueShapesDisjoint)) {
            context.$shapes.query().match("?parentShape", "sh:property", $currentShape).match("?parentShape", "sh:property", "?sibling").match("?sibling", "sh:qualifiedValueShape", "?siblingShape").filter(rdfquery.exprNotEquals("?siblingShape", $qualifiedValueShape)).addAllNodes("?siblingShape", siblingShapes);
        }
        return context.$data.query().path($this, rdfquery.toRDFQueryPath($path, context), "?value").filter(function (sol) {
            return nodeConformsToShape(sol.value, $qualifiedValueShape) &&
                !validateQualifiedConformsToASibling(sol.value, siblingShapes.toArray());
        }).getCount();
    }

    var validateQualifiedConformsToASibling = function (value, siblingShapes) {
        for (var i = 0; i < siblingShapes.length; i++) {
            if (nodeConformsToShape(value, siblingShapes[i])) {
                return true;
            }
        }
        return false;
    }

    var validateRootClass = function ($value, $rootClass) {
        return context.$data.query().path($value, {zeroOrMore: T("rdfs:subClassOf")}, $rootClass).hasSolution();
    }

    var validateStem = function ($value, $stem) {
        return $value.isURI() && $value.uri.startsWith($stem.lex);
    }

    var validateSubSetOf = function ($this, $subSetOf, $value) {
        return context.$data.query().match($this, $subSetOf, $value).hasSolution();
    }

    var validateUniqueLangProperty = function ($this, $uniqueLang, $path) {
        if (!T("true").equals($uniqueLang)) {
            return;
        }
        var map = {};
        context.$data.query().path($this, rdfquery.toRDFQueryPath($path, context), "?value").forEach(function (sol) {
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
        var shapes = context.$shapes.query().rdfListToArray($xone);
        var count = 0;
        for (var i = 0; i < shapes.length; i++) {
            if (nodeConformsToShape($value, shapes[i])) {
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
        else if (datatype.value === "http://www.w3.org/2001/XMLSchema#boolean") {
            return lex !== "true" && lex !== "false";
        } else {
            return true;
        }
    }


    context.functionRegistry.validateAnd = validateAnd;
    context.functionRegistry.validateClass = validateClass;
    context.functionRegistry.validateClosed = validateClosed;
    context.functionRegistry.validateClosedByTypesNode = validateClosedByTypesNode;
    context.functionRegistry.validateDatatype = validateDatatype;
    context.functionRegistry.validateDisjoint = validateDisjoint;
    context.functionRegistry.validateEqualsProperty = validateEqualsProperty;
    context.functionRegistry.validateEqualsNode = validateEqualsNode;
    context.functionRegistry.validateHasValueNode = validateHasValueNode;
    context.functionRegistry.validateHasValueProperty = validateHasValueProperty;
    context.functionRegistry.validateIn = validateIn;
    context.functionRegistry.validateLanguageIn = validateLanguageIn;
    context.functionRegistry.validateLessThanProperty = validateLessThanProperty;
    context.functionRegistry.validateLessThanOrEqualsProperty = validateLessThanOrEqualsProperty;
    context.functionRegistry.validateMaxCountProperty = validateMaxCountProperty;
    context.functionRegistry.validateMaxExclusive = validateMaxExclusive;
    context.functionRegistry.validateMaxInclusive = validateMaxInclusive;
    context.functionRegistry.validateMaxLength = validateMaxLength;
    context.functionRegistry.validateMinCountProperty = validateMinCountProperty;
    context.functionRegistry.validateMinExclusive = validateMinExclusive;
    context.functionRegistry.validateMinInclusive = validateMinInclusive;
    context.functionRegistry.validateMinLength = validateMinLength;
    context.functionRegistry.validateNodeKind = validateNodeKind;
    context.functionRegistry.validateNode = validateNode;
    context.functionRegistry.validateNonRecursiveProperty = validateNonRecursiveProperty;
    context.functionRegistry.validateNot = validateNot;
    context.functionRegistry.validateOr = validateOr;
    context.functionRegistry.validatePattern = validatePattern;
    context.functionRegistry.validatePrimaryKeyProperty = validatePrimaryKeyProperty;
    context.functionRegistry.validateQualifiedMaxCountProperty = validateQualifiedMaxCountProperty;
    context.functionRegistry.validateQualifiedMinCountProperty = validateQualifiedMinCountProperty;
    context.functionRegistry.validateQualifiedHelper = validateQualifiedHelper;
    context.functionRegistry.validateQualifiedConformsToASibling = validateQualifiedConformsToASibling;
    context.functionRegistry.validateRootClass = validateRootClass;
    context.functionRegistry.validateStem = validateStem;
    context.functionRegistry.validateSubSetOf = validateSubSetOf;
    context.functionRegistry.validateUniqueLangProperty = validateUniqueLangProperty;
    context.functionRegistry.validateXone = validateXone;

};

module.exports.registerDASH = registerDASH;
