package org.topbraid.shacl.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary for http://datashapes.org/dash
 */
public class DASH {

    public final static String BASE_URI = "http://datashapes.org/dash";
    
    public final static String NAME = "DASH Data Shapes Vocabulary";

    public final static String NS = BASE_URI + "#";

    public final static String PREFIX = "dash";


    public final static Resource FailureResult = ResourceFactory.createResource(NS + "FailureResult");
    
    public final static Resource FailureTestCaseResult = ResourceFactory.createResource(NS + "FailureTestCaseResult");

    public final static Resource FunctionTestCase = ResourceFactory.createResource(NS + "FunctionTestCase");

    public final static Resource GraphUpdate = ResourceFactory.createResource(NS + "GraphUpdate");

    public final static Resource GraphValidationTestCase = ResourceFactory.createResource(NS + "GraphValidationTestCase");

    public final static Resource InferencingTestCase = ResourceFactory.createResource(NS + "InferencingTestCase");

    public final static Resource None = ResourceFactory.createResource(NS + "None");

    public final static Resource QueryTestCase = ResourceFactory.createResource(NS + "QueryTestCase");

    public final static Resource SPARQLUpdateSuggestionGenerator = ResourceFactory.createResource(NS + "SPARQLUpdateSuggestionGenerator");

    public final static Resource SuccessResult = ResourceFactory.createResource(NS + "SuccessResult");
    
    public final static Resource SuccessTestCaseResult = ResourceFactory.createResource(NS + "SuccessTestCaseResult");

    public final static Resource TestCase = ResourceFactory.createResource(NS + "TestCase");

    public final static Resource ValidationTestCase = ResourceFactory.createResource(NS + "ValidationTestCase");


    public final static Property abstract_ = ResourceFactory.createProperty(NS + "abstract");
    
    public final static Property addedTriple = ResourceFactory.createProperty(NS + "addedTriple");

    public final static Property cachable = ResourceFactory.createProperty(NS + "cachable");
    
    public final static Property deletedTriple = ResourceFactory.createProperty(NS + "deletedTriple");
    
    public final static Property expectedResult = ResourceFactory.createProperty(NS + "expectedResult");

    public final static Property expression = ResourceFactory.createProperty(NS + "expression");

    public final static Property includeSuggestions = ResourceFactory.createProperty(NS + "includeSuggestions");
    
    public final static Property node = ResourceFactory.createProperty(NS + "node");
    
    public final static Property private_ = ResourceFactory.createProperty(NS + "private");
    
    public final static Property propertySuggestionGenerator = ResourceFactory.createProperty(NS + "propertySuggestionGenerator");

    public final static Property rootClass = ResourceFactory.createProperty(NS + "rootClass");
    
    public final static Property suggestion = ResourceFactory.createProperty(NS + "suggestion");
    
    public final static Property suggestionGenerator = ResourceFactory.createProperty(NS + "suggestionGenerator");

    public final static Property testCase = ResourceFactory.createProperty(NS + "testCase");

    public final static Property testGraph = ResourceFactory.createProperty(NS + "testGraph");


    public static String getURI() {
        return NS;
    }
}
