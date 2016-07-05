package org.topbraid.shacl.testcases;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.JenaUtil;

public class FunctionTestCaseType implements TestCaseType {


	@Override
	public Collection<TestCase> getTestCases(Model model, Resource graph) {
		List<TestCase> results = new LinkedList<TestCase>();
		for(Resource resource : JenaUtil.getAllInstances(model.getResource(DASH.FunctionTestCase.getURI()))) {
			results.add(new FunctionTestCase(graph, resource));
		}
		return results;
	}

	
	private static class FunctionTestCase extends TestCase {
		
		FunctionTestCase(Resource graph, Resource resource) {
			super(graph, resource);
		}

		
		@Override
		public void run(Model results) {
			Resource testCase = getResource();
			String expression = JenaUtil.getStringProperty(testCase, DASH.expression);
			Statement expectedResultS = testCase.getProperty(DASH.expectedResult);
			String queryString = "SELECT (" + expression + " AS ?result) WHERE {}";
			Query query = ARQFactory.get().createQuery(testCase.getModel(), queryString);
			QueryExecution qexec = ARQFactory.get().createQueryExecution(query, testCase.getModel());
			ResultSet rs = qexec.execSelect();
			try {
				if(!rs.hasNext()) {
					if(expectedResultS != null) {
						createFailure(results,
								"Expression returned no result, but expected: " + expectedResultS.getObject());
						return;
					}
				}
				else {
					RDFNode result = rs.next().get("result");
					if(expectedResultS == null) {
						if(result != null) {
							createFailure(results,
									"Expression returned a result, but none expected: " + result);
							return;
						}
					}
					else if(!expectedResultS.getObject().equals(result)) {
						createFailure(results,
								"Mismatching result. Expected: " + expectedResultS.getObject() + ". Found: " + result);
						return;
					}
				}
			}
			finally {
				qexec.close();
			}
			
			createResult(results, DASH.SuccessTestCaseResult);
		}
	}
}
