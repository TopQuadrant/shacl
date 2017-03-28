package org.topbraid.shacl.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class SHT {

	public static final String BASE_URI = "http://www.w3.org/ns/shacl-test";
	
	public static final String NS = BASE_URI + "#";
	
	
	public final static Property dataGraph = ResourceFactory.createProperty(NS + "dataGraph");
	
	public final static Resource proposed = ResourceFactory.createResource(NS + "proposed");
	
	public final static Property shapesGraph = ResourceFactory.createProperty(NS + "shapesGraph");
	
	public final static Resource Validate = ResourceFactory.createResource(NS + "Validate");
}
