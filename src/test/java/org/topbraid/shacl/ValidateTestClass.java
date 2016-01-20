package org.topbraid.shacl;

import org.topbraid.shacl.constraints.ModelConstraintValidator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class ValidateTestClass extends AbstractSHACLTestClass {

	public ValidateTestClass(Resource testResource) {
		super(testResource);
	}
	
	
	@Override
	protected void runTest() throws Throwable {
		Model results = ModelConstraintValidator.get().validateModel(createDataset(),
				getShapesGraphURI(), null, false, null);
		compareResults(results);
	}
}
