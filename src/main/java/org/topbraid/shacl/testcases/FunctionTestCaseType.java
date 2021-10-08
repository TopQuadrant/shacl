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

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.FileUtils;
import org.topbraid.jenax.functions.CurrentThreadFunctionRegistry;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.testcases.context.TestCaseContext;
import org.topbraid.shacl.testcases.context.TestCaseContextFactory;
import org.topbraid.shacl.vocabulary.DASH;

public class FunctionTestCaseType extends TestCaseType {
	
	private static List<TestCaseContextFactory> contextFactories = new LinkedList<>();
	
	public static void registerContextFactory(TestCaseContextFactory factory) {
		contextFactories.add(factory);
	}


	public FunctionTestCaseType() {
		super(DASH.FunctionTestCase);
	}


	@Override
	protected TestCase createTestCase(Resource graph, Resource resource) {
		return new FunctionTestCase(graph, resource);
	}


	private static class FunctionTestCase extends TestCase {
		
		FunctionTestCase(Resource graph, Resource resource) {
			super(graph, resource);
		}

		
		@Override
		public void run(Model results) {
			Resource testCase = getResource();
			
			Runnable tearDownCTFR = CurrentThreadFunctionRegistry.register(testCase.getModel());

			try {
				for(TestCaseContextFactory contextFactory : contextFactories) {
					TestCaseContext context = contextFactory.createContext();
					String expression = JenaUtil.getStringProperty(testCase, DASH.expression);
					Statement expectedResultS = testCase.getProperty(DASH.expectedResult);
					String queryString = "SELECT (" + expression + " AS ?result) WHERE {}";
					Query query = ARQFactory.get().createQuery(testCase.getModel(), queryString);
					context.setUpTestContext();
					try(QueryExecution qexec = ARQFactory.get().createQueryExecution(query, testCase.getModel())) {
					    ResultSet rs = qexec.execSelect();
					    if(!rs.hasNext()) {
					        if(expectedResultS != null) {
					            createFailure(results,
					                          "Expression returned no result, but expected: " + expectedResultS.getObject(),
					                          context);
					            return;
					        }
					    }
					    else {
					        RDFNode actual = rs.next().get("result");
					        if(expectedResultS == null) {
					            if(actual != null) {
					                createFailure(results,
					                              "Expression returned a result, but none expected: " + actual, context);
					                return;
					            }
					        }
					        else if(testCase.hasProperty(DASH.expectedResultIsTTL, JenaDatatypes.TRUE)) {
					        	Graph expectedGraph = parseGraph(expectedResultS.getObject());
					        	Graph actualGraph = parseGraph(actual);
					        	if(!expectedGraph.isIsomorphicWith(actualGraph)) {
						            createFailure(results,
					                          "Mismatching result graphs. Expected: " + expectedResultS.getObject() + ". Found: " + actual, context);
						            return;
					        	}
					        }
					        else if(!expectedResultS.getObject().equals(actual)) {
					            createFailure(results,
					                          "Mismatching result. Expected: " + expectedResultS.getObject() + ". Found: " + actual, context);
					            return;
					        }
					    }
					}
					finally {
						context.tearDownTestContext();
					}
				}
			}
			finally {
				tearDownCTFR.run();
			}
			
			createResult(results, DASH.SuccessTestCaseResult);
		}
		
		
		private Graph parseGraph(RDFNode node) {
			Model model = JenaUtil.createDefaultModel();
			if(node.isLiteral()) {
				String str = node.asLiteral().getLexicalForm();
				model.read(new ByteArrayInputStream(str.getBytes()), "urn:x:dummy", FileUtils.langTurtle);
			}
			return model.getGraph();
		}
	}
}
