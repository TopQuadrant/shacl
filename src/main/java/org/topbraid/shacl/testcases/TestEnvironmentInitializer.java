package org.topbraid.shacl.testcases;

/**
 * An abstraction of execution engines for dash:TestEnvironment.
 * Instances of this are managed by the TestEnvironmentInitializers singleton.
 * 
 * @author Holger Knublauch
 */
public interface TestEnvironmentInitializer {

	/**
	 * If this initializer feels responsible for the provided dash:TestEnvironment resource,
	 * it should make the necessary adjustments to the current working environment.
	 * @param testCase  the specific test case instance to execute
	 */
	void prepareTestEnvironment(TestCase testCase) throws Exception;
	
	
	/**
	 * If this initializer has made changes to the environment that should be undone,
	 * this method should restore the previous state.
	 * This is always called after execution of the test, regardless of whether an exception
	 * was thrown during the preparation step.
	 * Instances of this class are also getting their prepare and restore functions called
	 * in sequence, without interruptions.
	 * This means that the initializer instance may use private fields to store temp data.
	 * @param testCase  the specific test case instance to execute
	 */
	void restoreOriginalEnvironment(TestCase testCase) throws Exception;
}
