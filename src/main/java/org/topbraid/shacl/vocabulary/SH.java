/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
package org.topbraid.shacl.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;

/**
 * Vocabulary for <a href="http://www.w3.org/ns/shacl#">SHACL</a>
 *
 * @author Holger Knublauch
 */
public class SH {

    public final static String BASE_URI = "http://www.w3.org/ns/shacl#";

    public final static String NAME = "SHACL";

    public final static String NS = BASE_URI;

    public final static String PREFIX = "sh";


    public final static Resource AbstractResult = ResourceFactory.createResource(NS + "AbstractResult");

    public final static Resource AndConstraintComponent = ResourceFactory.createResource(NS + "AndConstraintComponent");

    public final static Resource BlankNode = ResourceFactory.createResource(NS + "BlankNode");

    public final static Resource BlankNodeOrIRI = ResourceFactory.createResource(NS + "BlankNodeOrIRI");

    public final static Resource BlankNodeOrLiteral = ResourceFactory.createResource(NS + "BlankNodeOrLiteral");

    public final static Resource ClassConstraintComponent = ResourceFactory.createResource(NS + "ClassConstraintComponent");

    public final static Resource ClosedConstraintComponent = ResourceFactory.createResource(NS + "ClosedConstraintComponent");

    public final static Resource Constraint = ResourceFactory.createResource(NS + "Constraint");

    public final static Resource ConstraintComponent = ResourceFactory.createResource(NS + "ConstraintComponent");

    public final static Resource DatatypeConstraintComponent = ResourceFactory.createResource(NS + "DatatypeConstraintComponent");

    public final static Resource DisjointConstraintComponent = ResourceFactory.createResource(NS + "DisjointConstraintComponent");

    public final static Resource EqualsConstraintComponent = ResourceFactory.createResource(NS + "EqualsConstraintComponent");

    public final static Resource HasValueConstraintComponent = ResourceFactory.createResource(NS + "HasValueConstraintComponent");

    public final static Resource InConstraintComponent = ResourceFactory.createResource(NS + "InConstraintComponent");

    public final static Resource Info = ResourceFactory.createResource(NS + "Info");

    public final static Resource IRI = ResourceFactory.createResource(NS + "IRI");

    public final static Resource IRIOrLiteral = ResourceFactory.createResource(NS + "IRIOrLiteral");

    public final static Resource LanguageInConstraintComponent = ResourceFactory.createResource(NS + "LanguageInConstraintComponent");

    public final static Resource LessThanConstraintComponent = ResourceFactory.createResource(NS + "LessThanConstraintComponent");

    public final static Resource LessThanOrEqualsConstraintComponent = ResourceFactory.createResource(NS + "LessThanOrEqualsConstraintComponent");

    public final static Resource Literal = ResourceFactory.createResource(NS + "Literal");

    public final static Resource MaxCountConstraintComponent = ResourceFactory.createResource(NS + "MaxCountConstraintComponent");

    public final static Resource MaxExclusiveConstraintComponent = ResourceFactory.createResource(NS + "MaxExclusiveConstraintComponent");

    public final static Resource MaxInclusiveConstraintComponent = ResourceFactory.createResource(NS + "MaxInclusiveConstraintComponent");

    public final static Resource MaxLengthConstraintComponent = ResourceFactory.createResource(NS + "MaxLengthConstraintComponent");

    public final static Resource MinCountConstraintComponent = ResourceFactory.createResource(NS + "MinCountConstraintComponent");

    public final static Resource MinExclusiveConstraintComponent = ResourceFactory.createResource(NS + "MinExclusiveConstraintComponent");

    public final static Resource MinInclusiveConstraintComponent = ResourceFactory.createResource(NS + "MinInclusiveConstraintComponent");

    public final static Resource MinLengthConstraintComponent = ResourceFactory.createResource(NS + "MinLengthConstraintComponent");

    public final static Resource NodeConstraintComponent = ResourceFactory.createResource(NS + "NodeConstraintComponent");

    public final static Resource NodeKindConstraintComponent = ResourceFactory.createResource(NS + "NodeKindConstraintComponent");

    public final static Resource NodeShape = ResourceFactory.createResource(NS + "NodeShape");

    public final static Resource NotConstraintComponent = ResourceFactory.createResource(NS + "NotConstraintComponent");

    public final static Resource OrConstraintComponent = ResourceFactory.createResource(NS + "OrConstraintComponent");

    public final static Resource Parameter = ResourceFactory.createResource(NS + "Parameter");

    public final static Resource Parameterizable = ResourceFactory.createResource(NS + "Parameterizable");

    public final static Resource PatternConstraintComponent = ResourceFactory.createResource(NS + "PatternConstraintComponent");

    public final static Resource PrefixDeclaration = ResourceFactory.createResource(NS + "PrefixDeclaration");

    public final static Resource PropertyGroup = ResourceFactory.createResource(NS + "PropertyGroup");

    public final static Resource PropertyShape = ResourceFactory.createResource(NS + "PropertyShape");

    public final static Resource PropertyConstraintComponent = ResourceFactory.createResource(NS + "PropertyConstraintComponent");

    public final static Resource QualifiedMaxCountConstraintComponent = ResourceFactory.createResource(NS + "QualifiedMaxCountConstraintComponent");

    public final static Resource QualifiedMinCountConstraintComponent = ResourceFactory.createResource(NS + "QualifiedMinCountConstraintComponent");

    public final static Resource ResultAnnotation = ResourceFactory.createResource(NS + "ResultAnnotation");

    public final static Resource Shape = ResourceFactory.createResource(NS + "Shape");

    public final static Resource SPARQLAskValidator = ResourceFactory.createResource(NS + "SPARQLAskValidator");

    public final static Resource SPARQLConstraint = ResourceFactory.createResource(NS + "SPARQLConstraint");

    public final static Resource SPARQLConstraintComponent = ResourceFactory.createResource(NS + "SPARQLConstraintComponent");

    public final static Resource SPARQLConstructRule = ResourceFactory.createResource(NS + "SPARQLConstructRule");

    public final static Resource SPARQLExecutable = ResourceFactory.createResource(NS + "SPARQLExecutable");

    public final static Resource SPARQLFunction = ResourceFactory.createResource(NS + "SPARQLFunction");

    public final static Resource SPARQLSelectValidator = ResourceFactory.createResource(NS + "SPARQLSelectValidator");

    public final static Resource SPARQLTarget = ResourceFactory.createResource(NS + "SPARQLTarget");

    public final static Resource SPARQLValuesDeriver = ResourceFactory.createResource(NS + "SPARQLValuesDeriver");

    public final static Resource UniqueLangConstraintComponent = ResourceFactory.createResource(NS + "UniqueLangConstraintComponent");

    public final static Resource ValidationReport = ResourceFactory.createResource(NS + "ValidationReport");

    public final static Resource ValidationResult = ResourceFactory.createResource(NS + "ValidationResult");

    public final static Resource Validator = ResourceFactory.createResource(NS + "Validator");

    public final static Resource Violation = ResourceFactory.createResource(NS + "Violation");

    public final static Resource Warning = ResourceFactory.createResource(NS + "Warning");

    public final static Resource XoneConstraintComponent = ResourceFactory.createResource(NS + "XoneConstraintComponent");


    public final static Property alternativePath = ResourceFactory.createProperty(NS + "alternativePath");

    public final static Property and = ResourceFactory.createProperty(NS + "and");

    public final static Property ask = ResourceFactory.createProperty(NS + "ask");

    public final static Property class_ = ResourceFactory.createProperty(NS + "class");

    public final static Property closed = ResourceFactory.createProperty(NS + "closed");

    public final static Property condition = ResourceFactory.createProperty(NS + "condition");

    public final static Property conforms = ResourceFactory.createProperty(NS + "conforms");

    public final static Property construct = ResourceFactory.createProperty(NS + "construct");

    public final static Property datatype = ResourceFactory.createProperty(NS + "datatype");

    public final static Property deactivated = ResourceFactory.createProperty(NS + "deactivated");

    public final static Property declare = ResourceFactory.createProperty(NS + "declare");

    public final static Property defaultValue = ResourceFactory.createProperty(NS + "defaultValue");

    public final static Property detail = ResourceFactory.createProperty(NS + "detail");

    public final static Property description = ResourceFactory.createProperty(NS + "description");

    public final static Property disjoint = ResourceFactory.createProperty(NS + "disjoint");

    public final static Property entailment = ResourceFactory.createProperty(NS + "entailment");

    public final static Property equals = ResourceFactory.createProperty(NS + "equals");

    public final static Property flags = ResourceFactory.createProperty(NS + "flags");

    public final static Property focusNode = ResourceFactory.createProperty(NS + "focusNode");

    public final static Property group = ResourceFactory.createProperty(NS + "group");

    public final static Property hasValue = ResourceFactory.createProperty(NS + "hasValue");

    public final static Property ignoredProperties = ResourceFactory.createProperty(NS + "ignoredProperties");

    public final static Property in = ResourceFactory.createProperty(NS + "in");

    public final static Property inversePath = ResourceFactory.createProperty(NS + "inversePath");

    public final static Property labelTemplate = ResourceFactory.createProperty(NS + "labelTemplate");

    public final static Property languageIn = ResourceFactory.createProperty(NS + "languageIn");

    public final static Property lessThan = ResourceFactory.createProperty(NS + "lessThan");

    public final static Property lessThanOrEquals = ResourceFactory.createProperty(NS + "lessThanOrEquals");

    public final static Property maxCount = ResourceFactory.createProperty(NS + "maxCount");

    public final static Property maxExclusive = ResourceFactory.createProperty(NS + "maxExclusive");

    public final static Property maxInclusive = ResourceFactory.createProperty(NS + "maxInclusive");

    public final static Property maxLength = ResourceFactory.createProperty(NS + "maxLength");

    public final static Property message = ResourceFactory.createProperty(NS + "message");

    public final static Property minCount = ResourceFactory.createProperty(NS + "minCount");

    public final static Property minExclusive = ResourceFactory.createProperty(NS + "minExclusive");

    public final static Property minInclusive = ResourceFactory.createProperty(NS + "minInclusive");

    public final static Property minLength = ResourceFactory.createProperty(NS + "minLength");

    public final static Property name = ResourceFactory.createProperty(NS + "name");

    public final static Property namespace = ResourceFactory.createProperty(NS + "namespace");

    public final static Property node = ResourceFactory.createProperty(NS + "node");

    public final static Property nodeKind = ResourceFactory.createProperty(NS + "nodeKind");

    public final static Property nodeValidator = ResourceFactory.createProperty(NS + "nodeValidator");

    public final static Property not = ResourceFactory.createProperty(NS + "not");

    public final static Property oneOrMorePath = ResourceFactory.createProperty(NS + "oneOrMorePath");

    public final static Property optional = ResourceFactory.createProperty(NS + "optional");

    public final static Property or = ResourceFactory.createProperty(NS + "or");

    public final static Property order = ResourceFactory.createProperty(NS + "order");

    public final static Property parameter = ResourceFactory.createProperty(NS + "parameter");

    public final static Property path = ResourceFactory.createProperty(NS + "path");

    public final static Property pattern = ResourceFactory.createProperty(NS + "pattern");

    public final static Property prefix = ResourceFactory.createProperty(NS + "prefix");

    public final static Property prefixes = ResourceFactory.createProperty(NS + "prefixes");

    public final static Property property = ResourceFactory.createProperty(NS + "property");

    public final static Property propertyValidator = ResourceFactory.createProperty(NS + "propertyValidator");

    public final static Property qualifiedMaxCount = ResourceFactory.createProperty(NS + "qualifiedMaxCount");

    public final static Property qualifiedMinCount = ResourceFactory.createProperty(NS + "qualifiedMinCount");

    public final static Property qualifiedValueShape = ResourceFactory.createProperty(NS + "qualifiedValueShape");

    public final static Property qualifiedValueShapesDisjoint = ResourceFactory.createProperty(NS + "qualifiedValueShapesDisjoint");

    public final static Property result = ResourceFactory.createProperty(NS + "result");

    public final static Property resultMessage = ResourceFactory.createProperty(NS + "resultMessage");

    public final static Property resultPath = ResourceFactory.createProperty(NS + "resultPath");

    public final static Property resultSeverity = ResourceFactory.createProperty(NS + "resultSeverity");

    public final static Property select = ResourceFactory.createProperty(NS + "select");

    public final static Property severity = ResourceFactory.createProperty(NS + "severity");

    public final static Property shapesGraph = ResourceFactory.createProperty(NS + "shapesGraph");

    public final static Property sourceConstraint = ResourceFactory.createProperty(NS + "sourceConstraint");

    public final static Property sourceConstraintComponent = ResourceFactory.createProperty(NS + "sourceConstraintComponent");

    public final static Property sourceShape = ResourceFactory.createProperty(NS + "sourceShape");

    public final static Property sparql = ResourceFactory.createProperty(NS + "sparql");

    public final static Property targetClass = ResourceFactory.createProperty(NS + "targetClass");

    public final static Property targetNode = ResourceFactory.createProperty(NS + "targetNode");

    public final static Property targetObjectsOf = ResourceFactory.createProperty(NS + "targetObjectsOf");

    public final static Property targetSubjectsOf = ResourceFactory.createProperty(NS + "targetSubjectsOf");

    public final static Property uniqueLang = ResourceFactory.createProperty(NS + "uniqueLang");

    public final static Property update = ResourceFactory.createProperty(NS + "update");

    public final static Property validator = ResourceFactory.createProperty(NS + "validator");

    public final static Property value = ResourceFactory.createProperty(NS + "value");

    public final static Property zeroOrMorePath = ResourceFactory.createProperty(NS + "zeroOrMorePath");

    public final static Property zeroOrOnePath = ResourceFactory.createProperty(NS + "zeroOrOnePath");


    // Advanced features

    public final static Resource ExpressionConstraintComponent = ResourceFactory.createResource(NS + "ExpressionConstraintComponent");

    public final static Resource Function = ResourceFactory.createResource(NS + "Function");

    public final static Resource JSConstraint = ResourceFactory.createResource(NS + "JSConstraint");

    public final static Resource JSConstraintComponent = ResourceFactory.createResource(NS + "JSConstraintComponent");

    public final static Resource JSExecutable = ResourceFactory.createResource(NS + "JSExecutable");

    public final static Resource JSFunction = ResourceFactory.createResource(NS + "JSFunction");

    public final static Resource JSLibrary = ResourceFactory.createResource(NS + "JSLibrary");

    public final static Resource JSRule = ResourceFactory.createResource(NS + "JSRule");

    public final static Resource JSTarget = ResourceFactory.createResource(NS + "JSTarget");

    public final static Resource JSTargetType = ResourceFactory.createResource(NS + "JSTargetType");

    public final static Resource JSValidator = ResourceFactory.createResource(NS + "JSValidator");

    public final static Resource Rule = ResourceFactory.createResource(NS + "Rule");

    public final static Resource Rules = ResourceFactory.createResource(NS + "Rules");

    public final static Resource SPARQLRule = ResourceFactory.createResource(NS + "SPARQLRule");

    public final static Resource Target = ResourceFactory.createResource(NS + "Target");

    public final static Resource this_ = ResourceFactory.createResource(NS + "this");

    public final static Resource TripleRule = ResourceFactory.createResource(NS + "TripleRule");


    public final static Property expression = ResourceFactory.createProperty(NS + "expression");

    public final static Property filterShape = ResourceFactory.createProperty(NS + "filterShape");

    public final static Property intersection = ResourceFactory.createProperty(NS + "intersection");

    public final static Property js = ResourceFactory.createProperty(NS + "js");

    public final static Property jsFunctionName = ResourceFactory.createProperty(NS + "jsFunctionName");

    public final static Property jsLibrary = ResourceFactory.createProperty(NS + "jsLibrary");

    public final static Property jsLibraryURL = ResourceFactory.createProperty(NS + "jsLibraryURL");

    public final static Property member = ResourceFactory.createProperty(NS + "member");

    public final static Property nodes = ResourceFactory.createProperty(NS + "nodes");

    public final static Property object = ResourceFactory.createProperty(NS + "object");

    public final static Property predicate = ResourceFactory.createProperty(NS + "predicate");

    public final static Property returnType = ResourceFactory.createProperty(NS + "returnType");

    public final static Property rule = ResourceFactory.createProperty(NS + "rule");

    public final static Property subject = ResourceFactory.createProperty(NS + "subject");

    public final static Property target = ResourceFactory.createProperty(NS + "target");

    public final static Property union = ResourceFactory.createProperty(NS + "union");


    // Features not in SHACL 1.0 but candidates for next release

    public final static Property count = ResourceFactory.createProperty(NS + "count");

    public final static Property desc = ResourceFactory.createProperty(NS + "desc");

    public final static Property distinct = ResourceFactory.createProperty(NS + "distinct");

    public final static Property else_ = ResourceFactory.createProperty(NS + "else");

    public final static Property exists = ResourceFactory.createProperty(NS + "exists");

    public final static Property groupConcat = ResourceFactory.createProperty(NS + "groupConcat");

    public final static Property if_ = ResourceFactory.createProperty(NS + "if");

    public final static Property limit = ResourceFactory.createProperty(NS + "limit");

    public final static Property max = ResourceFactory.createProperty(NS + "max");

    public final static Property min = ResourceFactory.createProperty(NS + "min");

    public final static Property minus = ResourceFactory.createProperty(NS + "minus");

    public final static Property offset = ResourceFactory.createProperty(NS + "offset");

    public final static Property orderBy = ResourceFactory.createProperty(NS + "orderBy");

    public final static Property separator = ResourceFactory.createProperty(NS + "separator");

    public final static Property sum = ResourceFactory.createProperty(NS + "sum");

    public final static Property then = ResourceFactory.createProperty(NS + "then");

    public final static Property values = ResourceFactory.createProperty(NS + "values");


    public static final Var currentShapeVar = Var.alloc("currentShape");

    public static final Var failureVar = Var.alloc("failure");

    public static final Var PATHVar = Var.alloc("PATH");

    public static final Var pathVar = Var.alloc(path.getLocalName());

    public static final Var shapesGraphVar = Var.alloc("shapesGraph");

    public static final Var thisVar = Var.alloc("this");

    public static final Var valueVar = Var.alloc("value");


    public final static String JS_DATA_VAR = "$data";

    public final static String JS_SHAPES_VAR = "$shapes";


    public static String getURI() {
        return NS;
    }
}
