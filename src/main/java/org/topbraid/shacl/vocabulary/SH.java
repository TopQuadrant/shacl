package org.topbraid.shacl.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;

/**
 * Vocabulary for http://www.w3.org/ns/shacl#
 * 
 * @author Holger Knublauch
 */
public class SH {

    public final static String BASE_URI = "http://www.w3.org/ns/shacl#";
    
    public final static String NAME = "SHACL";

    public final static String NS = BASE_URI;

    public final static String PREFIX = "sh";


    public final static Resource AbstractResult = ResourceFactory.createResource(NS + "AbstractResult");

    public final static Resource BlankNode = ResourceFactory.createResource(NS + "BlankNode");

    public final static Resource BlankNodeOrIRI = ResourceFactory.createResource(NS + "BlankNodeOrIRI");

    public final static Resource BlankNodeOrLiteral = ResourceFactory.createResource(NS + "BlankNodeOrLiteral");
    
    public final static Resource Constraint = ResourceFactory.createResource(NS + "Constraint");

    public final static Resource ConstraintComponent = ResourceFactory.createResource(NS + "ConstraintComponent");

    public final static Resource DerivedValuesConstraintComponent = ResourceFactory.createResource(NS + "DerivedValuesConstraintComponent");

    public final static Resource Function = ResourceFactory.createResource(NS + "Function");

    public final static Resource Info = ResourceFactory.createResource(NS + "Info");

    public final static Resource IRI = ResourceFactory.createResource(NS + "IRI");

    public final static Resource IRIOrLiteral = ResourceFactory.createResource(NS + "IRIOrLiteral");

    public final static Resource Literal = ResourceFactory.createResource(NS + "Literal");

    public final static Resource MaxCountConstraintComponent = ResourceFactory.createResource(NS + "MaxCountConstraintComponent");
    
    public final static Resource Parameter = ResourceFactory.createResource(NS + "Parameter");

    public final static Resource Parameterizable = ResourceFactory.createResource(NS + "Parameterizable");
    
    public final static Resource PrefixDeclaration = ResourceFactory.createResource(NS + "PrefixDeclaration");

    public final static Resource PropertyConstraint = ResourceFactory.createResource(NS + "PropertyConstraint");

    public final static Resource ResultAnnotation = ResourceFactory.createResource(NS + "ResultAnnotation");

    public final static Resource Shape = ResourceFactory.createResource(NS + "Shape");
    
    public final static Resource SPARQLAskValidator = ResourceFactory.createResource(NS + "SPARQLAskValidator");
    
    public final static Resource SPARQLConstraint = ResourceFactory.createResource(NS + "SPARQLConstraint");

    public final static Resource SPARQLExecutable = ResourceFactory.createResource(NS + "SPARQLExecutable");

    public final static Resource SPARQLFunction = ResourceFactory.createResource(NS + "SPARQLFunction");
    
    public final static Resource SPARQLSelectValidator = ResourceFactory.createResource(NS + "SPARQLSelectValidator");
    
    public final static Resource SPARQLTarget = ResourceFactory.createResource(NS + "SPARQLTarget");
    
    public final static Resource SPARQLValuesDeriver = ResourceFactory.createResource(NS + "SPARQLValuesDeriver");
    
    public final static Resource Target = ResourceFactory.createResource(NS + "Target");
    
    public final static Resource ValidationResult = ResourceFactory.createResource(NS + "ValidationResult");
    
    public final static Resource Validator = ResourceFactory.createResource(NS + "Validator");

    public final static Resource Violation = ResourceFactory.createResource(NS + "Violation");

    public final static Resource Warning = ResourceFactory.createResource(NS + "Warning");


    public final static Property alternativePath = ResourceFactory.createProperty(NS + "alternativePath");
    
    public final static Property and = ResourceFactory.createProperty(NS + "and");

    public final static Property ask = ResourceFactory.createProperty(NS + "ask");

    public final static Property class_ = ResourceFactory.createProperty(NS + "class");

    public final static Property construct = ResourceFactory.createProperty(NS + "construct");

    public final static Property datatype = ResourceFactory.createProperty(NS + "datatype");
    
    public final static Property deactivated = ResourceFactory.createProperty(NS + "deactivated");

    public final static Property declare = ResourceFactory.createProperty(NS + "declare");

    public final static Property defaultValue = ResourceFactory.createProperty(NS + "defaultValue");

    public final static Property detail = ResourceFactory.createProperty(NS + "detail");

    public final static Property derivedValues = ResourceFactory.createProperty(NS + "derivedValues");

    public final static Property description = ResourceFactory.createProperty(NS + "description");

    public final static Property entailment = ResourceFactory.createProperty(NS + "entailment");

    public final static Property focusNode = ResourceFactory.createProperty(NS + "focusNode");

    public final static Property hasValue = ResourceFactory.createProperty(NS + "hasValue");
    
    public final static Property in = ResourceFactory.createProperty(NS + "in");

    public final static Property inversePath = ResourceFactory.createProperty(NS + "inversePath");

    public final static Property labelTemplate = ResourceFactory.createProperty(NS + "labelTemplate");

    public final static Property maxCount = ResourceFactory.createProperty(NS + "maxCount");

    public final static Property maxExclusive = ResourceFactory.createProperty(NS + "maxExclusive");

    public final static Property maxInclusive = ResourceFactory.createProperty(NS + "maxInclusive");

    public final static Property member = ResourceFactory.createProperty(NS + "member");

    public final static Property message = ResourceFactory.createProperty(NS + "message");

    public final static Property minCount = ResourceFactory.createProperty(NS + "minCount");

    public final static Property minExclusive = ResourceFactory.createProperty(NS + "minExclusive");

    public final static Property minInclusive = ResourceFactory.createProperty(NS + "minInclusive");

    public final static Property name = ResourceFactory.createProperty(NS + "name");

    public final static Property namespace = ResourceFactory.createProperty(NS + "namespace");

    public final static Property nodeKind = ResourceFactory.createProperty(NS + "nodeKind");
    
    public final static Property not = ResourceFactory.createProperty(NS + "not");

    public final static Property oneOrMorePath = ResourceFactory.createProperty(NS + "oneOrMorePath");

    public final static Property optional = ResourceFactory.createProperty(NS + "optional");

    public final static Property or = ResourceFactory.createProperty(NS + "or");

    public final static Property order = ResourceFactory.createProperty(NS + "order");

    public final static Property parameter = ResourceFactory.createProperty(NS + "parameter");

    public final static Property path = ResourceFactory.createProperty(NS + "path");

    public final static Property predicate = ResourceFactory.createProperty(NS + "predicate");

    public final static Property prefix = ResourceFactory.createProperty(NS + "prefix");

    public final static Property prefixes = ResourceFactory.createProperty(NS + "prefixes");

    public final static Property property = ResourceFactory.createProperty(NS + "property");

    public final static Property propertyValidator = ResourceFactory.createProperty(NS + "propertyValidator");

    public final static Property resultMessage = ResourceFactory.createProperty(NS + "resultMessage");

    public final static Property resultPath = ResourceFactory.createProperty(NS + "resultPath");

    public final static Property resultSeverity = ResourceFactory.createProperty(NS + "resultSeverity");

    public final static Property returnType = ResourceFactory.createProperty(NS + "returnType");

    public final static Property select = ResourceFactory.createProperty(NS + "select");

    public final static Property severity = ResourceFactory.createProperty(NS + "severity");
    
    public final static Property shape = ResourceFactory.createProperty(NS + "shape");

    public final static Property shapesGraph = ResourceFactory.createProperty(NS + "shapesGraph");

    public final static Property shapeValidator = ResourceFactory.createProperty(NS + "shapeValidator");

    public final static Property sourceConstraint = ResourceFactory.createProperty(NS + "sourceConstraint");
    
    public final static Property sourceConstraintComponent = ResourceFactory.createProperty(NS + "sourceConstraintComponent");

    public final static Property sourceShape = ResourceFactory.createProperty(NS + "sourceShape");

    public final static Property sparql = ResourceFactory.createProperty(NS + "sparql");
    
    public final static Property target = ResourceFactory.createProperty(NS + "target");
    
    public final static Property targetClass = ResourceFactory.createProperty(NS + "targetClass");

    public final static Property targetNode = ResourceFactory.createProperty(NS + "targetNode");
    
    public final static Property targetObjectsOf = ResourceFactory.createProperty(NS + "targetObjectsOf");
    
    public final static Property targetSubjectsOf = ResourceFactory.createProperty(NS + "targetSubjectsOf");

    public final static Property update = ResourceFactory.createProperty(NS + "update");

    public final static Property validator = ResourceFactory.createProperty(NS + "validator");

    public final static Property value = ResourceFactory.createProperty(NS + "value");

    public final static Property zeroOrMorePath = ResourceFactory.createProperty(NS + "zeroOrMorePath");

    public final static Property zeroOrOnePath = ResourceFactory.createProperty(NS + "zeroOrOnePath");

	
	public static final Var currentShapeVar = Var.alloc("currentShape");

	public static final Var failureVar = Var.alloc("failure");

	public static final Var focusNodeVar = Var.alloc(focusNode.getLocalName());

	public static final Var PATHVar = Var.alloc("PATH");

	public static final Var pathVar = Var.alloc(path.getLocalName());

	public static final Var predicateVar = Var.alloc(predicate.getLocalName());

	public static final Var shapesGraphVar = Var.alloc("shapesGraph");
	
	public static final Var thisVar = Var.alloc("this");
	
	public static final Var valueVar = Var.alloc("value");


    public static String getURI() {
        return NS;
    }
}
