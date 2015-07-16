package org.topbraid.shacl;

import org.topbraid.shacl.constraints.ModelConstraintValidator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class ValidateTestClass extends AbstractSHACLTestClass {

	public ValidateTestClass(Resource testResource) {
		super(testResource);
	}
	
	
	public void testRun() throws Exception {
		Model results = ModelConstraintValidator.get().validateModel(createDataset(),
				getShapesGraphURI(), null, false, null);
		compareResults(results);
	}
}
