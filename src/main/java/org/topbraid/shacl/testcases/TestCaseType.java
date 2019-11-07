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

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.vocabulary.SH;

/**
 * An abstract class to hook types of test cases into the TopBraid Test Cases framework.
 * 
 * @author Holger Knublauch
 */
public abstract class TestCaseType {
	private final Resource testCaseClass;

	public TestCaseType(Resource testCaseClass) {
		this.testCaseClass = testCaseClass;
	}

	/**
	 * Can be overridden to check the properties of the resource
	 * and return false if it is to be excluded from the list
	 * of cases to test.
	 */
	protected boolean filterTestCase(Resource possibleTestCase) {
		return !possibleTestCase.hasProperty(SH.deactivated, JenaDatatypes.TRUE);
	}

	protected abstract TestCase createTestCase(Resource graph, Resource resource);

	/**
	 * Gets all test case resources from a given Model that this type can handle.
	 * @param model  the Model containing the test case
	 * @param graph  the URI resource of model
	 * @return the rest cases
	 */
	public Collection<TestCase> getTestCases(Model model, Resource graph) {
		return JenaUtil.getAllInstances(testCaseClass.inModel(model))
				.stream()
				.filter(this::filterTestCase)
				.map(resource -> createTestCase(graph, resource))
				.collect(Collectors.toList());
	}
}
