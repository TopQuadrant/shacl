package org.topbraid.shacl;

import java.net.URI;
import java.util.UUID;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.topbraid.shacl.arq.SHACLFunctions;
import org.topbraid.shacl.constraints.ModelConstraintValidator;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.JenaUtil;

public class ValidationExample {

	/**
	 * Loads an example SHACL file and validates all constraints.
	 * This file can also be used as a starting point for your own custom applications.
	 */
	public static void main(String[] args) throws Exception {
		
		// Load the main data model
		Model dataModel = JenaUtil.createMemoryModel();
		dataModel.read(ValidationExample.class.getResourceAsStream("/sh/tests/core/property/class-001.test.ttl"), "urn:dummy", FileUtils.langTurtle);
		
		// Load the shapes Model (here, includes the dataModel because that has shape definitions too)
		Model shaclModel = SHACLSystemModel.getSHACLModel();
		MultiUnion unionGraph = new MultiUnion(new Graph[] {
			shaclModel.getGraph(),
			dataModel.getGraph()
		});
		Model shapesModel = ModelFactory.createModelForGraph(unionGraph);

		// Make sure all sh:Functions are registered
		SHACLFunctions.registerFunctions(shapesModel);
		
		// Create Dataset that contains both the main query model and the shapes model
		// (here, using a temporary URI for the shapes graph)
		URI shapesGraphURI = URI.create("urn:x-shacl-shapes-graph:" + UUID.randomUUID().toString());
		Dataset dataset = ARQFactory.get().getDataset(dataModel);
		dataset.addNamedModel(shapesGraphURI.toString(), shapesModel);
		
		// Run the validator
		Model results = new ModelConstraintValidator().validateModel(dataset, shapesGraphURI, null, true, null, null).getModel();
		
		// Print violations
		System.out.println(ModelPrinter.get().print(results));
	}
}