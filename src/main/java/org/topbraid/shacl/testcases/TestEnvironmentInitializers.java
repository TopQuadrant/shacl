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
	 * @param initializer  the initializer to install
	 */
	public static void add(TestEnvironmentInitializer initializer) {
		initializers.add(initializer);
	}
	
	
	/**
	 * Calls all initializers for the given TestCase before the test has been run.
	 * @param testCase  the TestCase to initialize the environment for
	 * @throws Exception if something went horribly wrong
	 */
	public static void initTestEnvironment(TestCase testCase) throws Exception {
		for(TestEnvironmentInitializer initializer : initializers) {
			initializer.prepareTestEnvironment(testCase);
		}
	}
	
	
	/**
	 * Calls all initializers for the given TestCase after the test has been run.
	 * @param testCase  the TestCase to restore the old environment for
	 * @throws Exception if something went horribly wrong
	 */
	public static void restoreOriginalEnvironment(TestCase testCase) throws Exception {
		for(TestEnvironmentInitializer initializer : initializers) {
			initializer.restoreOriginalEnvironment(testCase);
		}
	}
}
