package org.topbraid.shacl.testcases;

import java.util.Collection;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

/**
 * An interface to hook types of test cases into the TopBraid Test Cases framework.
 * 
 * @author Holger Knublauch
 */
public interface TestCaseType {

	/**
	 * Gets all test case resources from a given Model that this type can handle.
	 * @param model  the Model containing the test case
	 * @param graph  the URI resource of model
	 * @return the rest cases
	 */
	Collection<TestCase> getTestCases(Model model, Resource graph);
}
