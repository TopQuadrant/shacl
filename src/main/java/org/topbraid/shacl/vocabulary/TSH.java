package org.topbraid.shacl.vocabulary;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary for http://topbraid.org/shacl/tsh
 * 
 * @author Holger Knublauch
 */
public class TSH {

    public final static String BASE_URI = "http://topbraid.org/shacl/tsh";
    
    public final static String NS = BASE_URI + "#";

    public final static String PREFIX = "tsh";

    public final static Resource FailureResult = ResourceFactory.createResource(NS + "FailureResult");

    public final static Resource SuccessResult = ResourceFactory.createResource(NS + "SuccessResult");
}
