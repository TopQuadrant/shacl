package org.topbraid.shacl;

import java.io.InputStream;
import java.net.URI;
import java.util.UUID;

import org.topbraid.shacl.arq.SHACLFunctions;
import org.topbraid.shacl.constraints.ModelConstraintValidator;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.JenaUtil;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;

import junit.framework.TestCase;

public class ValidateSquareExampleTest extends TestCase {

	/**
	 * Loads the SHACL Square example file and validates all constraints.
	 * This test case can also be used as a starting point for your own custom applications.
	 */
	public void testSHACLSquare() throws InterruptedException {
		
		// Load the main data model
		Model dataModel = JenaUtil.createMemoryModel();
		dataModel.read(getClass().getResourceAsStream("/shaclsquare.ttl"), "urn:dummy", FileUtils.langTurtle);
		
		// Load the shapes Model (here, includes the dataModel because that has templates in it)
		Model shaclModel = JenaUtil.createDefaultModel();
		InputStream is = getClass().getResourceAsStream("/etc/shacl.ttl");
		shaclModel.read(is, SH.BASE_URI, FileUtils.langTurtle);
		MultiUnion unionGraph = new MultiUnion(new Graph[] {
			shaclModel.getGraph(),
			dataModel.getGraph()
		});
		Model shapesModel = ModelFactory.createModelForGraph(unionGraph);

		// Set filtered to true to exclude schema-level constraint validation
		boolean filtered = false;
		if(!filtered) {
			// If we want to validate the shapes too, then additional type triples are expected
			dataModel = SHACLUtil.withDefaultValueTypeInferences(shapesModel);
		}
		
		// Make sure all sh:Functions are registered
		SHACLFunctions.registerFunctions(shapesModel);
		
		// Create Dataset that contains both the main query model and the shapes model
		// (here, using a temporary URI for the shapes graph)
		URI shapesGraphURI = URI.create("urn:x-shacl-shapes-graph:" + UUID.randomUUID().toString());
		Dataset dataset = ARQFactory.get().getDataset(dataModel);
		dataset.addNamedModel(shapesGraphURI.toString(), shapesModel);
		
		// Run the validator
		Model results = ModelConstraintValidator.get().validateModel(dataset, shapesGraphURI, null, filtered, null);
		
		// Uncomment following line to see the validation results
		// System.out.println(ModelPrinter.get().print(results));
		
		// Expecting 2 constraint violations (9 triples each)
		assertEquals(18, results.size());
	}
}
