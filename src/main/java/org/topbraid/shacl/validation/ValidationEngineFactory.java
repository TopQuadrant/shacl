package org.topbraid.shacl.validation;

import java.net.URI;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;

/**
 * A singleton that can be used to produce new ValidationEngines.
 * 
 * Implementations may install their own subclass to make some default modifications
 * such as attaching a monitor or label function.
 * 
 * @author Holger Knublauch
 */
public class ValidationEngineFactory {

	private static ValidationEngineFactory singleton = new ValidationEngineFactory();
	
	public static ValidationEngineFactory get() {
		return singleton;
	}
	
	public static void set(ValidationEngineFactory value) {
		singleton = value;
	}
	
	
	/**
	 * Constructs a new ValidationEngine.
	 * @param dataset  the Dataset to operate on
	 * @param shapesGraphURI  the URI of the shapes graph (must be in the dataset)
	 * @param shapesGraph  the ShapesGraph with the shapes to validate against
	 * @param report  the sh:ValidationReport object in the results Model, or null to create a new one
	 * @return a new ValidationEngine
	 */
	public ValidationEngine create(Dataset dataset, URI shapesGraphURI, ShapesGraph shapesGraph, Resource report) {
		return new ValidationEngine(dataset, shapesGraphURI, shapesGraph, report);
	}
}
