package org.topbraid.shacl.testcases;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.ExceptionUtil;
import org.topbraid.spin.util.JenaUtil;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

public class QueryTestCaseType implements TestCaseType {

	public static String createResultSetJSON(String queryString, Model model) {
		Query query = ARQFactory.get().createQuery(model, queryString);
		QueryExecution qexec = ARQFactory.get().createQueryExecution(query, model);
		ResultSet actualResults = qexec.execSelect();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ResultSetFormatter.outputAsJSON(os, actualResults);
		qexec.close();
		try {
			return os.toString("UTF-8");
		} 
		catch (UnsupportedEncodingException e) {
			throw ExceptionUtil.throwUnchecked(e);
		}
	}


	@Override
	public Collection<TestCase> getTestCases(Model model, Resource graph) {
		List<TestCase> results = new LinkedList<TestCase>();
		for(Resource resource : JenaUtil.getAllInstances(model.getResource(DASH.QueryTestCase.getURI()))) {
			results.add(new QueryTestCase(graph, resource));
		}
		return results;
	}

	
	private static class QueryTestCase extends TestCase {
		
		QueryTestCase(Resource graph, Resource resource) {
			super(graph, resource);
		}

		
		@Override
		public void run(Model results) throws Exception {
			Resource testCase = getResource();
			String queryString = JenaUtil.getStringProperty(testCase, SH.sparql);
			Model model = testCase.getModel();
			String actual = createResultSetJSON(queryString, model);
			JsonObject actualJSON = JSON.parse(actual);
			JsonObject expectedJSON = JSON.parse(JenaUtil.getStringProperty(testCase, DASH.expectedResult));
			if(actualJSON.equals(expectedJSON)) {
				createResult(results, DASH.SuccessTestCaseResult);
			}
			else {
				createFailure(results, "Mismatching result set");
			}
		}
	}
}
