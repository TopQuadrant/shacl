package org.topbraid.shacl.vocabulary;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class EDG {
	public final static String BASE_URI = "http://edg.topbraid.solutions/model";
    
    public final static String NAME = "EDG";

    public final static String NS = BASE_URI + "/";

    public final static String PREFIX = "edg";
    
    public final static Resource GlossaryTerm = ResourceFactory.createResource(NS + "GlossaryTerm");
    public final static Resource RequirementsViewpoint = ResourceFactory.createResource(NS + "RequirementsViewpoint");
    public final static Resource BigDataAsset = ResourceFactory.createResource(NS + "BigDataAsset");
    public final static Resource DataAsset = ResourceFactory.createResource(NS + "DataAsset");
    public final static Resource DataValueRule = ResourceFactory.createResource(NS + "DataValueRule");
    public final static Resource Datatype = ResourceFactory.createResource(NS + "Datatype");
    public final static Resource EnumerationViewpoint = ResourceFactory.createResource(NS + "EnumerationViewpoint");
    public final static Resource EnterpriseAsset = ResourceFactory.createResource(NS + "EnterpriseAsset");
    public final static Resource TechnicalAsset = ResourceFactory.createResource(NS + "TechnicalAsset");
    public final static Resource LineageModel = ResourceFactory.createResource(NS + "LineageModel");
    
    public static String getURI() {
        return NS;
    }
}
