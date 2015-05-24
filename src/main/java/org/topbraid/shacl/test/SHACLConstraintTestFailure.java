package org.topbraid.shacl.test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A negative result of running SHACLTestCases.
 *  
 * @author Holger Knublauch
 */
public class SHACLConstraintTestFailure {
	
	private Model actualModel;
	
	private Model expectedModel;
	
	private Resource testCase;
	

	public SHACLConstraintTestFailure(Resource testCase, Model expectedModel, Model actualModel) {
		this.actualModel = actualModel;
		this.expectedModel = expectedModel;
		this.testCase = testCase;
	}
	
	
	public Model getActualModel() {
		return actualModel;
	}
	
	
	public Model getExpectedModel() {
		return expectedModel;
	}
	
	
	public Resource getTestCase() {
		return testCase;
	}
	
	
	public String toString() {
		return "Failure of SHACL Constraint Test " + getTestCase() + "\n\nExpected:\n" + 
				ModelPrinter.get().print(getExpectedModel()) + "\n\nActual:\n" + 
				ModelPrinter.get().print(getActualModel());
	}
}
