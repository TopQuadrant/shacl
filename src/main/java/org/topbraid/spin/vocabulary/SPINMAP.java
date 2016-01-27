package org.topbraid.spin.vocabulary;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

/**
 * Vocabulary for http://spinrdf.org/spinmap
 *
 * Automatically generated with TopBraid Composer.
 */
public class SPINMAP {

    public static final String BASE_URI = "http://spinrdf.org/spinmap";

    public static final String NS = BASE_URI + "#";

    public static final String PREFIX = "spinmap";

    public static final String TARGET_PREDICATE = "targetPredicate";
    
    public static final String SOURCE_PREDICATE = "sourcePredicate";

    public static final Resource Context = ResourceFactory.createResource(NS + "Context");

    public static final Resource Mapping = ResourceFactory.createResource(NS + "Mapping");

    public static final Resource Mapping_0_1 = ResourceFactory.createResource(NS + "Mapping-0-1");

    public static final Resource Mapping_1 = ResourceFactory.createResource(NS + "Mapping-1");

    public static final Resource Mapping_1_1 = ResourceFactory.createResource(NS + "Mapping-1-1");

    public static final Resource Mapping_1_1_Inverse = ResourceFactory.createResource(NS + "Mapping-1-1-Inverse");

    public static final Resource Mapping_1_Path_1 = ResourceFactory.createResource(NS + "Mapping-1-Path-1");
    
    public static final Resource Mapping_2_1 = ResourceFactory.createResource(NS + "Mapping-2-1");

    public static final Resource SplitMapping_1_1 = ResourceFactory.createResource(NS + "SplitMapping-1-1");

    public static final Resource TargetFunction = ResourceFactory.createResource(NS + "TargetFunction");

    public static final Resource TargetFunctions = ResourceFactory.createResource(NS + "TargetFunctions");

    public static final Resource TransformationFunction = ResourceFactory.createResource(NS + "TransformationFunction");

    public static final Resource TransformationFunctions = ResourceFactory.createResource(NS + "TransformationFunctions");

    public static final Property context = ResourceFactory.createProperty(NS + "context");

    public static final Resource equals = ResourceFactory.createResource(NS + "equals");

    public static final Property expression = ResourceFactory.createProperty(NS + "expression");

    public static final Property function = ResourceFactory.createProperty(NS + "function");

    public static final Property inverseExpression = ResourceFactory.createProperty(NS + "inverseExpression");

    public static final Property postRule = ResourceFactory.createProperty(NS + "postRule");

    public static final Property predicate = ResourceFactory.createProperty(NS + "predicate");

    public static final Property prepRule = ResourceFactory.createProperty(NS + "prepRule");

    public static final Property rule = ResourceFactory.createProperty(NS + "rule");

    public static final Property separator = ResourceFactory.createProperty(NS + "separator");

    public static final Property shortLabel = ResourceFactory.createProperty(NS + "shortLabel");

    public static final Property source = ResourceFactory.createProperty(NS + "source");

    public static final Property sourceClass = ResourceFactory.createProperty(NS + "sourceClass");

    public static final Property sourcePath = ResourceFactory.createProperty(NS + "sourcePath");

    public static final Property sourcePredicate1 = ResourceFactory.createProperty(NS + SOURCE_PREDICATE + "1");

    public static final Property sourcePredicate2 = ResourceFactory.createProperty(NS + SOURCE_PREDICATE + "2");

    public static final Property sourcePredicate3 = ResourceFactory.createProperty(NS + SOURCE_PREDICATE + "3");

    public static final Resource sourceVariable = ResourceFactory.createResource(NS + "_source");

    public static final Property suggestion_0_1 = ResourceFactory.createProperty(NS + "suggestion-0-1");

    public static final Property suggestion_1_1 = ResourceFactory.createProperty(NS + "suggestion-1-1");

    public static final Property suggestionScore = ResourceFactory.createProperty(NS + "suggestionScore");

    public static final Property target = ResourceFactory.createProperty(NS + "target");

    public static final Property targetClass = ResourceFactory.createProperty(NS + "targetClass");

    public static final Property targetPredicate1 = ResourceFactory.createProperty(NS + TARGET_PREDICATE + "1");
    
    public static final Property targetPredicate2 = ResourceFactory.createProperty(NS + TARGET_PREDICATE + "2");

    public static final Resource targetResource = ResourceFactory.createResource(NS + "targetResource");

    public static final Property template = ResourceFactory.createProperty(NS + "template");

    public static final Property type = ResourceFactory.createProperty(NS + "type");

    public static final Property value = ResourceFactory.createProperty(NS + "value");

    public static final Property value1 = ResourceFactory.createProperty(NS + "value1");

    public static final Property value2 = ResourceFactory.createProperty(NS + "value2");
    
    public static final Property condition = ResourceFactory.createProperty(NS + "condition");
    
    public static boolean exists(Model model) {
    	return model.contains(model.getResource(SPINMAP.BASE_URI), RDF.type, OWL.Ontology);
    }
    
    
    public static String getURI() {
        return NS;
    }
}
