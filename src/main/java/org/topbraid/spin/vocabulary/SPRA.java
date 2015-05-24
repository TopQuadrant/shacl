package org.topbraid.spin.vocabulary;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary for http://spinrdf.org/spra
 *
 * @author Holger Knublauch
 */
public class SPRA {

    public final static String BASE_URI = "http://spinrdf.org/spra";

    public final static String NS = BASE_URI + "#";

    public final static String PREFIX = "spra";


    public final static Resource Table = ResourceFactory.createResource(NS + "Table");


    public static String getURI() {
        return NS;
    }
}
