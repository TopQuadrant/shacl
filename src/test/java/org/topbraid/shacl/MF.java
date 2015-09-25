package org.topbraid.shacl;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class MF {

	public final static String BASE_URI = "http://www.w3.org/2001/sw/DataAccess/tests/test-manifest";

	public final static String NS = BASE_URI + "#";
	
	public final static String PREFIX = "mf";
	
	
	public final static Resource Manifest = ResourceFactory.createResource(NS + "Manifest");
	
	public final static Property action = ResourceFactory.createProperty(NS + "action");
	
	public final static Property entries = ResourceFactory.createProperty(NS + "entries");
	
	public final static Property include = ResourceFactory.createProperty(NS + "include");
	
	public final static Property name = ResourceFactory.createProperty(NS + "name");
	
	public final static Property result = ResourceFactory.createProperty(NS + "result");
	
	public final static Property status = ResourceFactory.createProperty(NS + "status");
}
