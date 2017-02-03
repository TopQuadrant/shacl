package org.topbraid.shacl.testcases;

import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public abstract class TestCase implements Comparable<TestCase> {

	private Resource graph;
	
	private Resource resource;
	
	
	public TestCase(Resource graph, Resource resource) {
		this.graph = graph;
		this.resource = resource;
	}
	
	
	@Override
	public int compareTo(TestCase other) {
		Resource env1 = JenaUtil.getResourceProperty(getResource(), DASH.testEnvironment);
		Resource env2 = JenaUtil.getResourceProperty(other.getResource(), DASH.testEnvironment);
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
		
		Resource e1 = JenaUtil.getResourceProperty(getResource(), DASH.testEnvironment);
		Resource e2 = JenaUtil.getResourceProperty(other.getResource(), DASH.testEnvironment);
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
