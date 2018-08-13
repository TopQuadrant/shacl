package org.topbraid.shacl.validation;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.engine.ShapesGraph;

import java.net.URI;

/**
 * Interface to construct a ValidationEngine.
 */
public interface ValidationEngineConstructor {
    /**
     * Constructs a new ValidationEngine.
     * @param dataset  the Dataset to operate on
     * @param shapesGraphURI  the URI of the shapes graph (must be in the dataset)
     * @param shapesGraph  the ShapesGraph with the shapes to validate against
     * @param report  the sh:ValidationReport object in the results Model, or null to create a new one
     * @return a new ValidationEngine
     */
    ValidationEngine create(Dataset dataset, URI shapesGraphURI, ShapesGraph shapesGraph, Resource report);
}
