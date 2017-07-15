package org.topbraid.shacl;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.spin.util.JenaUtil;

public class ValidationExample {

	/**
	 * Loads an example SHACL file and validates all focus nodes against all shapes.
	 */
	public static void main(String[] args) throws Exception {
		
		// Load the main data model
		Model dataModel = JenaUtil.createMemoryModel();
		dataModel.read(ValidationExample.class.getResourceAsStream("/sh/tests/core/property/class-001.test.ttl"), "urn:dummy", FileUtils.langTurtle);
		
		// Perform the validation of everything, using the data model
		// also as the shapes model - you may have them separated
		Resource report = ValidationUtil.validateModel(dataModel, dataModel, true);
		
		// Print violations
		System.out.println(ModelPrinter.get().print(report.getModel()));
	}
}