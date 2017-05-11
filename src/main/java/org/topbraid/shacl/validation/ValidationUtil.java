package org.topbraid.shacl.validation;

import java.net.URI;
import java.util.UUID;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.shacl.arq.SHACLFunctions;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.engine.filters.ExcludeMetaShapesFilter;
import org.topbraid.shacl.util.SHACLSystemModel;
import org.topbraid.shacl.vocabulary.TOSH;
import org.topbraid.spin.arq.ARQFactory;

/**
 * Convenience methods to perform SHACL validation.
 * 
 * @author Holger Knublauch
 */
public class ValidationUtil {

	/**
	 * Validates a given data Model against all shapes from a given shapes Model.
	 * If the shapesModel does not include the system graph triples then these will be added.
	 * Entailment regimes are applied prior to validation.
	 * @param dataModel  the data Model
	 * @param shapesModel  the shapes Model
	 * @param validateShapes  true to also validate any shapes in the data Model (false is faster)
	 * @return an instance of sh:ValidationReport in a results Model
	 */
	public static Resource validateModel(Model dataModel, Model shapesModel, boolean validateShapes) {
		
		// Ensure that the SHACL, DASH and TOSH graphs are present in the shapes Model
		if(!shapesModel.contains(TOSH.hasShape, RDF.type, (RDFNode)null)) { // Heuristic
			Model unionModel = SHACLSystemModel.getSHACLModel();
			MultiUnion unionGraph = new MultiUnion(new Graph[] {
				unionModel.getGraph(),
				shapesModel.getGraph()
			});
			shapesModel = ModelFactory.createModelForGraph(unionGraph);
		}

		// Make sure all sh:Functions are registered
		SHACLFunctions.registerFunctions(shapesModel);
		
		// Create Dataset that contains both the data model and the shapes model
		// (here, using a temporary URI for the shapes graph)
		URI shapesGraphURI = URI.create("urn:x-shacl-shapes-graph:" + UUID.randomUUID().toString());
		Dataset dataset = ARQFactory.get().getDataset(dataModel);
		dataset.addNamedModel(shapesGraphURI.toString(), shapesModel);

		ShapesGraph shapesGraph = new ShapesGraph(shapesModel);
		if(!validateShapes) {
			shapesGraph.setShapeFilter(new ExcludeMetaShapesFilter());
		}
		ValidationEngine engine = ValidationEngineFactory.get().create(dataset, shapesGraphURI, shapesGraph, null);
		try {
			engine.applyEntailments();
			return engine.validateAll();
		}
		catch(InterruptedException ex) {
			return null;
		}
	}
}
