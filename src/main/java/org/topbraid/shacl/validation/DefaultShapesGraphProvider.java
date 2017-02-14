package org.topbraid.shacl.validation;

import java.net.URI;

import org.apache.jena.query.Dataset;

/**
 * A singleton used by ResourceConstraintValidator (and thus the tosh:hasShape function)
 * to deliver a default shapes graph if none has been provided in the context.
 * This is to support calling tosh:hasShape outside of a validation engine.
 * 
 * By default, this throws an exception, but within TopBraid products this uses other
 * heuristics to find the most suitable shapes graph.
 * 
 * @author Holger Knublauch
 */
public class DefaultShapesGraphProvider {

	private static DefaultShapesGraphProvider singleton = new DefaultShapesGraphProvider();
	
	public static DefaultShapesGraphProvider get() {
		return singleton;
	}
	
	public static void set(DefaultShapesGraphProvider value) {
		singleton = value;
	}
	
	
	public URI getDefaultShapesGraphURI(Dataset dataset) {
		throw new IllegalArgumentException("Cannot invoke node validation without a shapes graph URI");
	}
}
