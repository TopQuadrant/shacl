/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
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
