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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.datatypes.xsd.impl.RDFjson;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.topbraid.jenax.functions.CurrentThreadFunctionRegistry;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.ExceptionUtil;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.multifunctions.MultiFunctions;
import org.topbraid.shacl.testcases.context.TestCaseContext;
import org.topbraid.shacl.testcases.context.TestCaseContextFactory;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

public class QueryTestCaseType extends TestCaseType {
	
	private static List<TestCaseContextFactory> contextFactories = new LinkedList<>();
	
	public static void registerContextFactory(TestCaseContextFactory factory) {
		contextFactories.add(factory);
	}

	public static String createResultSetJSON(String queryString, Model model) {
		Runnable tearDownCTFR = CurrentThreadFunctionRegistry.register(model);
		try {
			Query query = ARQFactory.get().createQuery(model, queryString);
			try(QueryExecution qexec = ARQFactory.get().createQueryExecution(query, model)) {
    			ResultSet actualResults = qexec.execSelect();
    			ByteArrayOutputStream os = new ByteArrayOutputStream();
    			ResultSetFormatter.outputAsJSON(os, actualResults);
                return os.toString("UTF-8");
			}
		} 
		catch (UnsupportedEncodingException e) {
			throw ExceptionUtil.throwUnchecked(e);
		}
		finally {
			tearDownCTFR.run();
		}
	}


	public QueryTestCaseType() {
		super(DASH.QueryTestCase);
	}

	
	@Override
	protected TestCase createTestCase(Resource graph, Resource resource) {
		return new QueryTestCase(graph, resource);
	}

	
	private static class QueryTestCase extends TestCase {
		
		QueryTestCase(Resource graph, Resource resource) {
			super(graph, resource);
		}

		
		@Override
		public void run(Model results) throws Exception {
			
			Iterable<String> tempURIs = MultiFunctions.registerAllTemp(getResource().getModel());
			try {
				Resource testCase = getResource();
				String queryString = JenaUtil.getStringProperty(testCase, SH.select);
				Model model = testCase.getModel();
				JsonObject expectedJSON = JSON.parse(JenaUtil.getStringProperty(testCase, DASH.expectedResult));
				
				for(TestCaseContextFactory contextFactory : contextFactories) {
					TestCaseContext context = contextFactory.createContext();
					context.setUpTestContext();
					try {
						String actual = createResultSetJSON(queryString, model);
						JsonObject actualJSON = JSON.parse(actual);
						if(!actualJSON.equals(expectedJSON)) {
							System.err.println("---- Test failure: "+testCase);
							System.err.println(queryString);
							System.err.println("---- Expected ----");
							System.err.println(expectedJSON);
							System.err.println("---- Actual ----");
							System.err.println(actual);
							System.err.println("----");
							Resource failure = createFailure(results, "Mismatching query result set", context);
							failure.addProperty(DASH.actualResult, ResourceFactory.createTypedLiteral(actualJSON.toString(), RDFjson.rdfJSON));
							return;
						}
					}
					finally {
						context.tearDownTestContext();
					}
				}
				createResult(results, DASH.SuccessTestCaseResult);
			}
			finally {
				tempURIs.forEach(uri -> MultiFunctions.unregister(uri));
			}
		}
	}
}
