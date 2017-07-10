// Functions implementing the validators of SHACL-JS
// Also include validators for the constraint components of the DASH namespace

// There is no validator for sh:property as this is expected to be
// natively implemented by the surrounding engine.

var rdfquery = require("./rdfquery");
var T = rdfquery.T;


var registerDASH = function(shaclValidator) {

    var depth = 0;

    var compareNodes = function (node1, node2) {
        // TODO: Does not handle the case where nodes cannot be compared
        return rdfquery.compareTerms(node1, node2);
    };

    var nodeConformsToShape = function (focusNode, shapeNode) {
        var localEngine = new ValidationEngine(shaclValidator, true);
        var shape = shaclValidator.shapesGraph.getShape(shapeNode);
        try {
            depth++;
            return !localEngine.validateNodeAgainstShape(focusNode, shape);
        }
        finally {
            depth--;
        }
    };

    var $shapes =  function() {
        return shaclValidator.rdfShapes;
    };
    var $data = function() {
        return shaclValidator.rdfData;
    };
    var XSDIntegerTypes = new rdfquery.NodeSet();
    XSDIntegerTypes.add(T("xsd:integer"));

    var XSDDecimalTypes = new rdfquery.NodeSet();
    XSDDecimalTypes.addAll(XSDIntegerTypes.toArray());
    XSDDecimalTypes.add(T("xsd:decimal"));
    XSDDecimalTypes.add(T("xsd:float"));

    var validateAnd = function ($value, $and) {
        var shapes = rdfquery($shapes()).rdfListToArray($and);
        for (var i = 0; i < shapes.length; i++) {
            if (!nodeConformsToShape($value, shapes[i])) {
                return false;
            }
        }
        return true;
    };

    var validateClass = function ($value, $class) {
        return rdfquery.isInstanceOf($value, $class, shaclValidator);
    };

    var validateClosed = function ($value, $closed, $ignoredProperties, $currentShape) {
        if (!T("true").equals($closed)) {
            return;
        }
        var allowed = $shapes().query().match($currentShape, "sh:property", "?propertyShape").match("?propertyShape", "sh:path", "?path").filter(function (solution) {
            return solution.path.isURI()
        }).getNodeSet("?path");
        if ($ignoredProperties) {
            allowed.addAll(rdfquery($shapes()).rdfListToArray($ignoredProperties));
        }
        var results = [];
        $data().query().match($value, "?predicate", "?object").filter(function (sol) {
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
        var allowedProperties = new rdfquery.NodeSet();
        $data().query().match($this, "rdf:type", "?directType").path("?directType", {zeroOrMore: T("rdfs:subClassOf")}, "?type").forEachNode("?type", function (type) {
            $shapes().query().match(type, "sh:property", "?pshape").match("?pshape", "sh:path", "?path").filter(function (sol) {
                return sol.path.isURI()
            }).addAllNodes("?path", allowedProperties);
        });
        $data().query().match($this, "?predicate", "?object").filter(function (sol) {
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
        return !$data().query().match($this, $disjoint, $value).hasSolution();
    };

    var validateEqualsProperty = function ($this, $path, $equals) {
        var results = [];
        var path = rdfquery.toRDFQueryPath($path, shaclValidator);
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
        var count = $data().query().path($this, rdfquery.toRDFQueryPath($path, shaclValidator), $hasValue).getCount();
        return count > 0;
    };

    var validateIn = function ($value, $in) {
        var set = new rdfquery.NodeSet();
        set.addAll(new rdfquery($shapes(), shaclValidator).rdfListToArray($in));
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
        var ls = new rdfquery($shapes(), shaclValidator).rdfListToArray($languageIn);
        for (var i = 0; i < ls.length; i++) {
            if (lang.startsWith(ls[i].lex)) {
                return true;
            }
        }
        return false;
    }

    var validateLessThanProperty = function ($this, $path, $lessThan) {
        var results = [];
        $data().query().path($this, rdfquery.toRDFQueryPath($path, shaclValidator), "?value").match($this, $lessThan, "?otherValue").forEach(function (sol) {
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
        $data().query().path($this, rdfquery.toRDFQueryPath($path, shaclValidator), "?value").match($this, $lessThanOrEquals, "?otherValue").forEach(function (sol) {
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
        var count = $data().query().path($this, rdfquery.toRDFQueryPath($path, shaclValidator), "?any").getCount();
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
        var count = $data().query().path($this, rdfquery.toRDFQueryPath($path, shaclValidator), "?any").getCount();
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
        return nodeConformsToShape($value, $node);
    }

    var validateNonRecursiveProperty = function ($this, $path, $nonRecursive) {
        if (T("true").equals($nonRecursive)) {
            if ($data().query().path($this, rdfquery.toRDFQueryPath($path, shaclValidator), $this).hasSolution()) {
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
        var shapes = rdfquery($shapes()).rdfListToArray($or);
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
        if ($data().query().path($this, rdfquery.toRDFQueryPath($path, shaclValidator), null).getCount() != 1) {
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
            $shapes().query().match("?parentShape", "sh:property", $currentShape).match("?parentShape", "sh:property", "?sibling").match("?sibling", "sh:qualifiedValueShape", "?siblingShape").filter(exprNotEquals("?siblingShape", $qualifiedValueShape)).addAllNodes("?siblingShape", siblingShapes);
        }
        return $data().query().path($this, rdfquery.toRDFQueryPath($path, shaclValidator), "?value").filter(function (sol) {
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
        return $data().query().path($value, {zeroOrMore: T("rdfs:subClassOf")}, $rootClass).hasSolution();
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
        $data().query().path($this, rdfquery.toRDFQueryPath($path, shaclValidator), "?value").forEach(function (sol) {
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
        var shapes = rdfquery($shapes()).rdfListToArray($xone);
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
        else {
            return true;
        }
    }


    shaclValidator.functionRegistry.validateAnd = validateAnd;
    shaclValidator.functionRegistry.validateClass = validateClass;
    shaclValidator.functionRegistry.validateClosed = validateClosed;
    shaclValidator.functionRegistry.validateClosedByTypesNode = validateClosedByTypesNode;
    shaclValidator.functionRegistry.validateDatatype = validateDatatype;
    shaclValidator.functionRegistry.validateDisjoint = validateDisjoint;
    shaclValidator.functionRegistry.validateEqualsProperty = validateEqualsProperty;
    shaclValidator.functionRegistry.validateHasValueNode = validateHasValueNode;
    shaclValidator.functionRegistry.validateHasValueProperty = validateHasValueProperty;
    shaclValidator.functionRegistry.validateIn = validateIn;
    shaclValidator.functionRegistry.validateLanguageIn = validateLanguageIn;
    shaclValidator.functionRegistry.validateLessThanProperty = validateLessThanProperty;
    shaclValidator.functionRegistry.validateLessThanOrEqualsProperty = validateLessThanOrEqualsProperty;
    shaclValidator.functionRegistry.validateMaxCountProperty = validateMaxCountProperty;
    shaclValidator.functionRegistry.validateMaxExclusive = validateMaxExclusive;
    shaclValidator.functionRegistry.validateMaxInclusive = validateMaxInclusive;
    shaclValidator.functionRegistry.validateMaxLength = validateMaxLength;
    shaclValidator.functionRegistry.validateMinCountProperty = validateMinCountProperty;
    shaclValidator.functionRegistry.validateMinExclusive = validateMinExclusive;
    shaclValidator.functionRegistry.validateMinInclusive = validateMinInclusive;
    shaclValidator.functionRegistry.validateMinLength = validateMinLength;
    shaclValidator.functionRegistry.validateNodeKind = validateNodeKind;
    shaclValidator.functionRegistry.validateNode = validateNode;
    shaclValidator.functionRegistry.validateNonRecursiveProperty = validateNonRecursiveProperty;
    shaclValidator.functionRegistry.validateNot = validateNot;
    shaclValidator.functionRegistry.validateOr = validateOr;
    shaclValidator.functionRegistry.validatePattern = validatePattern;
    shaclValidator.functionRegistry.validatePrimaryKeyProperty = validatePrimaryKeyProperty;
    shaclValidator.functionRegistry.validateQualifiedMaxCountProperty = validateQualifiedMaxCountProperty;
    shaclValidator.functionRegistry.validateQualifiedMinCountProperty = validateQualifiedMinCountProperty;
    shaclValidator.functionRegistry.validateQualifiedHelper = validateQualifiedHelper;
    shaclValidator.functionRegistry.validateQualifiedConformsToASibling = validateQualifiedConformsToASibling;
    shaclValidator.functionRegistry.validateRootClass = validateRootClass;
    shaclValidator.functionRegistry.validateStem = validateStem;
    shaclValidator.functionRegistry.validateSubSetOf = validateSubSetOf;
    shaclValidator.functionRegistry.validateUniqueLangProperty = validateUniqueLangProperty;
    shaclValidator.functionRegistry.validateXone = validateXone;

};

module.exports.registerDASH = registerDASH;
