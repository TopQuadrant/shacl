package org.topbraid.shacl.js;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Vocabulary for http://www.w3.org/ns/shacljs
 *
 * Automatically generated with TopBraid Composer.
 */
public class SHJS {

    public final static String BASE_URI = SH.BASE_URI;

    public final static String NS = BASE_URI;


    public final static Resource JSConstraint = ResourceFactory.createResource(NS + "JSConstraint");

    public final static Resource JSConstraintComponent = ResourceFactory.createResource(NS + "JSConstraintComponent");

    public final static Resource JSExecutable = ResourceFactory.createResource(NS + "JSExecutable");

    public final static Resource JSFunction = ResourceFactory.createResource(NS + "JSFunction");

    public final static Resource JSLibrary = ResourceFactory.createResource(NS + "JSLibrary");

    public final static Resource JSValidator = ResourceFactory.createResource(NS + "JSValidator");

    
    public final static Property js = ResourceFactory.createProperty(NS + "js");
    
    public final static Property jsFunctionName = ResourceFactory.createProperty(NS + "jsFunctionName");

    public final static Property jsLibrary = ResourceFactory.createProperty(NS + "jsLibrary");

    public final static Property jsLibraryURL = ResourceFactory.createProperty(NS + "jsLibraryURL");


    public static String getURI() {
        return NS;
    }
}
