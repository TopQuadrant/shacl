package org.topbraid.shacl.testcases;

import java.util.LinkedList;
import java.util.List;

/**
 * Registry of TestCaseTypes, serving as a plugin mechanism.
 * 
 * @author Holger Knublauch
 */
public class TestCaseTypes {
	
	private static List<TestCaseType> types = new LinkedList<TestCaseType>();
	static {
		types.add(new FunctionTestCaseType());
		types.add(new GraphValidationTestCaseType());
		//types.add(new InferencingTestCaseType());
		types.add(new JSTestCaseType());
		types.add(new QueryTestCaseType());
	}
	
	public static void add(TestCaseType type) {
		types.add(type);
	}
	
	public static Iterable<TestCaseType> getTypes() {
		return types;
	}
}
