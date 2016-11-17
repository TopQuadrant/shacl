package org.topbraid.shacl.testcases;

import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public abstract class TestCase {

	private Resource graph;
	
	private Resource resource;
	
	
	public TestCase(Resource graph, Resource resource) {
		this.graph = graph;
		this.resource = resource;
	}
	
	
	public Resource createFailure(Model results, String message) {
		Resource failure = createResult(results, DASH.FailureTestCaseResult);
		failure.addProperty(SH.resultMessage, message);
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
