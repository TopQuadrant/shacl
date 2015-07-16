package org.topbraid.spin.vocabulary;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Constants to access the arg: namespace.
 * 
 * @author Holger Knublauch
 */
public class ARG {

	public final static String BASE_URI = "http://spinrdf.org/arg";
	
	public final static String NS = BASE_URI + "#";
	
	public final static String PREFIX = "arg";
	
	
	public final static Resource property = ResourceFactory.createResource(NS + "property");
	
	public final static Resource maxCount = ResourceFactory.createResource(NS + "maxCount");
	
	public final static Resource minCount = ResourceFactory.createResource(NS + "minCount");
}
