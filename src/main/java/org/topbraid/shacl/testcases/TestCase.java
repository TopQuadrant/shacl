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
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
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
		return getResource().getURI().compareTo(other.getResource().getURI());
	}


	public Resource createFailure(Model results, String message) {
		return createFailure(results, message, (RDFNode) null);
	}


	public Resource createFailure(Model results, String message, RDFNode actualResult) {
		Resource failure = createResult(results, DASH.FailureTestCaseResult);
		failure.addProperty(SH.resultMessage, message);
		if(actualResult != null) {
			failure.addProperty(DASH.actualResult, actualResult);
		}
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
}
