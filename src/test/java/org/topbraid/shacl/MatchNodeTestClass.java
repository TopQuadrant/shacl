package org.topbraid.shacl;

import org.topbraid.shacl.constraints.ResourceConstraintValidator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public class MatchNodeTestClass extends AbstractSHACLTestClass {

	public MatchNodeTestClass(Resource testResource) {
		super(testResource);
	}
	
	
	@Override
	protected void runTest() throws Throwable {
		Model shapesModel = getShapesModel();
		Model dataModel = getDataModel();
		Resource action = testResource.getPropertyResourceValue(MF.action);
		Resource focusNode = action.getPropertyResourceValue(SHT.node).inModel(dataModel);
		Resource shape = action.getPropertyResourceValue(SHT.shape).inModel(shapesModel);
		Model results = ResourceConstraintValidator.get().validateNodeAgainstShape(createDataset(), 
				getShapesGraphURI(), focusNode.asNode(), shape.asNode(), null, null);
		compareResults(results);
	}
}
