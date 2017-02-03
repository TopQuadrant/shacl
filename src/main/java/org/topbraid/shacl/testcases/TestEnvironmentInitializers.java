package org.topbraid.shacl.testcases;

import java.util.LinkedList;
import java.util.List;

/**
 * A singleton managing the installed TestEnvironmentInitializer instances.
 * 
 * @author Holger Knublauch
 */
public class TestEnvironmentInitializers {

	private static List<TestEnvironmentInitializer> initializers = new LinkedList<>();
	
	
	/**
	 * Installs a new initializer.
	 * @param initializer
	 */
	public static void add(TestEnvironmentInitializer initializer) {
		initializers.add(initializer);
	}
	
	
	/**
	 * Calls all initializers for the given TestCase before the test has been run.
	 * @param testCase  the TestCase to initialize the environment for
	 */
	public static void initTestEnvironment(TestCase testCase) throws Exception {
		for(TestEnvironmentInitializer initializer : initializers) {
			initializer.prepareTestEnvironment(testCase);
		}
	}
	
	
	/**
	 * Calls all initializers for the given TestCase after the test has been run.
	 * @param testCase  the TestCase to restore the old environment for
	 */
	public static void restoreOriginalEnvironment(TestCase testCase) throws Exception {
		for(TestEnvironmentInitializer initializer : initializers) {
			initializer.restoreOriginalEnvironment(testCase);
		}
	}
}
