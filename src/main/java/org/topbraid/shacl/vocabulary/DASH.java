package org.topbraid.shacl.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary for http://datashapes.org/dash
 *
 * Automatically generated with TopBraid Composer.
 */
public class DASH {

    public final static String BASE_URI = "http://datashapes.org/dash";
    
    public final static String NAME = "DASH Data Shapes Vocabulary";

    public final static String NS = BASE_URI + "#";

    public final static String PREFIX = "dash";


    public final static Resource FailureResult = ResourceFactory.createResource(NS + "FailureResult");
    
    public final static Resource FailureTestCaseResult = ResourceFactory.createResource(NS + "FailureTestCaseResult");

    public final static Resource FunctionTestCase = ResourceFactory.createResource(NS + "FunctionTestCase");

    public final static Resource GraphValidationTestCase = ResourceFactory.createResource(NS + "GraphValidationTestCase");

    public final static Resource InferencingTestCase = ResourceFactory.createResource(NS + "InferencingTestCase");

    public final static Resource PrimaryKeyPropertyConstraint = ResourceFactory.createResource(NS + "PrimaryKeyPropertyConstraint");

    public final static Resource QueryTestCase = ResourceFactory.createResource(NS + "QueryTestCase");

    public final static Resource SuccessResult = ResourceFactory.createResource(NS + "SuccessResult");
    
    public final static Resource SuccessTestCaseResult = ResourceFactory.createResource(NS + "SuccessTestCaseResult");

    public final static Resource TestCase = ResourceFactory.createResource(NS + "TestCase");

    public final static Resource ValidationTestCase = ResourceFactory.createResource(NS + "ValidationTestCase");


    public final static Property cachable = ResourceFactory.createProperty(NS + "cachable");
    
    public final static Property expectedResult = ResourceFactory.createProperty(NS + "expectedResult");

    public final static Property expression = ResourceFactory.createProperty(NS + "expression");

    public final static Property node = ResourceFactory.createProperty(NS + "node");
    
    public final static Property private_ = ResourceFactory.createProperty(NS + "private");

    public final static Property testCase = ResourceFactory.createProperty(NS + "testCase");

    public final static Property testGraph = ResourceFactory.createProperty(NS + "testGraph");

    public final static Property uriStart = ResourceFactory.createProperty(NS + "uriStart");


    public static String getURI() {
        return NS;
    }
}
