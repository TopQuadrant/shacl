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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.shacl.testcases.context.TestCaseContext;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

public abstract class TestCase implements Comparable<TestCase> {

	private Resource graph;
	
	private Resource resource;
	
	
	public TestCase(Resource graph, Resource resource) {
		this.graph = graph;
		this.resource = resource;
	}
	
	
	@Override
	public int compareTo(TestCase other) {
		Resource env1 = getResource().getPropertyResourceValue(DASH.testEnvironment);
		Resource env2 = other.getResource().getPropertyResourceValue(DASH.testEnvironment);
		if(env1 != null) {
			String uri1 = env1.getURI();
			if(env2 != null) {
				String uri2 = env2.getURI();
				int c = uri1.compareTo(uri2);
				if(c != 0) {
					return c;
				}
				else {
					Integer m1 = getResource().hasProperty(DASH.testModifiesEnvironment, JenaDatatypes.TRUE) ? 1 : 0;
					Integer m2 = other.getResource().hasProperty(DASH.testModifiesEnvironment, JenaDatatypes.TRUE) ? 1 : 0;
					int m = m1.compareTo(m2);
					if(m != 0) {
						return m;
					}
				}
			}
			else {
				return 1;
			}
		}
		else if(env2 != null) {
			return -1;
		}
		return getResource().getURI().compareTo(other.getResource().getURI());
	}


	public Resource createFailure(Model results, String message) {
		Resource failure = createResult(results, DASH.FailureTestCaseResult);
		failure.addProperty(SH.resultMessage, message);
		return failure;
	}


	public Resource createFailure(Model results, String message, TestCaseContext context) {
		Resource failure = createResult(results, DASH.FailureTestCaseResult);
		failure.addProperty(SH.resultMessage, message + " (executed with " + context + ")");
		return failure;
	}
	
	
	protected Resource createResult(Model results, Resource type) {
		Resource result = results.createResource(type);
		result.addProperty(DASH.testCase, resource);
		result.addProperty(DASH.testGraph, graph);
		return result;
	}
	
	
	public Resource getGraph() {
		return graph;
	}
	
	
	public Resource getResource() {
		return resource;
	}
	
	
	public abstract void run(Model results) throws Exception;
	
	
	public boolean usesDifferentEnvironmentFrom(TestCase other) {
		
		if(getResource().hasProperty(DASH.testModifiesEnvironment)) {
			return true;
		}
		if(other.getResource().hasProperty(DASH.testModifiesEnvironment)) {
			return true;
		}
		
		Resource e1 = getResource().getPropertyResourceValue(DASH.testEnvironment);
		Resource e2 = other.getResource().getPropertyResourceValue(DASH.testEnvironment);
		if(e1 != null && e2 != null) {
			return !e1.equals(e2);
		}
		else if(e1 == null && e2 == null) {
			return false;
		}
		else {
			return true;
		}
	}
}
