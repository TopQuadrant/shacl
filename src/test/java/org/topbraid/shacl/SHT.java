package org.topbraid.shacl;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class SHT {

	public final static String BASE_URI = "http://www.w3.org/ns/shacl/test-suite";

	public final static String NS = BASE_URI + "#";
	
	public final static String PREFIX = "mf";
	
	
	public final static Resource ConvertSchemaSyntax = ResourceFactory.createResource(NS + "ConvertSchemaSyntax");
	
	public final static Resource Failure = ResourceFactory.createResource(NS + "Failure");
	
	public final static Resource MatchNodeShape = ResourceFactory.createResource(NS + "MatchNodeShape");
	
	public final static Resource NonWellFormedSchema = ResourceFactory.createResource(NS + "NonWellFormedSchema");
	
	public final static Resource proposed = ResourceFactory.createResource(NS + "proposed");
	
	public final static Resource SHACLC = ResourceFactory.createResource(NS + "SHACLC");
	
	public final static Resource TURTLE = ResourceFactory.createResource(NS + "TURTLE");
	
	public final static Resource Validate = ResourceFactory.createResource(NS + "Validate");
	
	public final static Resource WellFormedSchema = ResourceFactory.createResource(NS + "WellFormedSchema");
	
	public final static Property data = ResourceFactory.createProperty(NS + "data");
	
	public final static Property data_format = ResourceFactory.createProperty(NS + "data-format");
	
	public final static Property node = ResourceFactory.createProperty(NS + "node");
	
	public final static Property schema = ResourceFactory.createProperty(NS + "schema");
	
	public final static Property schema_format = ResourceFactory.createProperty(NS + "schema-format");
	
	public final static Property shape = ResourceFactory.createProperty(NS + "shape");
}
