package org.topbraid.shacl.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary for http://topbraid.org/tosh
 */
public class TOSH {

    public final static String BASE_URI = "http://topbraid.org/tosh";
    
    public final static String NAME = "TopBraid Data Shapes Vocabulary";

    public final static String NS = BASE_URI + "#";

    public final static String PREFIX = "tosh";

    
    public final static Resource evalExpr = ResourceFactory.createResource(NS + "evalExpr");
    
    public final static Resource hasShape = ResourceFactory.createResource(NS + "hasShape");
    
    public final static Resource isInTargetOf = ResourceFactory.createResource(NS + "isInTargetOf");
    
    public final static Resource targetContains = ResourceFactory.createResource(NS + "targetContains");

    public final static Property editWidget = ResourceFactory.createProperty(NS + "editWidget");

    public final static Property searchWidget = ResourceFactory.createProperty(NS + "searchWidget");

    public final static Property useDeclaredDatatype = ResourceFactory.createProperty(NS + "useDeclaredDatatype");

    public final static Property viewWidget = ResourceFactory.createProperty(NS + "viewWidget");


    public static String getURI() {
        return NS;
    }
}
