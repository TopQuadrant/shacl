package org.topbraid.shacl.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class MF {

	public static final String BASE_URI = "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest";
	
	public static final String NS = BASE_URI + "#";
			
			
	public final static Property action = ResourceFactory.createProperty(NS + "action");

	public final static Property entries = ResourceFactory.createProperty(NS + "entries");

	public final static Property include = ResourceFactory.createProperty(NS + "include");
	
	public final static Resource Manifest = ResourceFactory.createResource(NS + "Manifest");
	
	public final static Property result = ResourceFactory.createProperty(NS + "result");
	
	public final static Property status = ResourceFactory.createProperty(NS + "status");
}
