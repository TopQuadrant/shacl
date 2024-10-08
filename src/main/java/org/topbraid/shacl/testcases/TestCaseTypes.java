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
 * Registry of TestCaseTypes, serving as a plugin mechanism.
 * 
 * @author Holger Knublauch
 */
public class TestCaseTypes {
	
	private static List<TestCaseType> types = new LinkedList<TestCaseType>();
	static {
		types.add(new FunctionTestCaseType());
		types.add(new GraphValidationTestCaseType());
		types.add(new InferencingTestCaseType());
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
