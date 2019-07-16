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
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.topbraid.jenax.functions.CurrentThreadFunctionRegistry;
import org.topbraid.jenax.functions.CurrentThreadFunctions;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.vocabulary.DASH;

public class JSTestCaseType implements TestCaseType {

	@Override
	public Collection<TestCase> getTestCases(Model model, Resource graph) {
		List<TestCase> results = new LinkedList<TestCase>();
		for(Resource resource : JenaUtil.getAllInstances(model.getResource(DASH.JSTestCase.getURI()))) {
			results.add(new JSTestCase(graph, resource));
		}
		return results;
	}


	
	private static class JSTestCase extends TestCase {
		
		JSTestCase(Resource graph, Resource resource) {
			super(graph, resource);
		}

		
		@Override
		public void run(Model results) {
			Resource testCase = getResource();
			
			FunctionRegistry oldFR = FunctionRegistry.get();
			CurrentThreadFunctionRegistry threadFR = new CurrentThreadFunctionRegistry(oldFR);
			FunctionRegistry.set(ARQ.getContext(), threadFR);

			CurrentThreadFunctions old = CurrentThreadFunctionRegistry.register(testCase.getModel());
			Statement expectedResultS = testCase.getProperty(DASH.expectedResult);
			String queryString = "SELECT (<" + getResource() + ">() AS ?result) WHERE {}";
			Query query = ARQFactory.get().createQuery(testCase.getModel(), queryString);
			try(QueryExecution qexec = ARQFactory.get().createQueryExecution(query, testCase.getModel())) {
			    ResultSet rs = qexec.execSelect();
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
				CurrentThreadFunctionRegistry.unregister(old);
				FunctionRegistry.set(ARQ.getContext(), oldFR);
			}
			
			createResult(results, DASH.SuccessTestCaseResult);
		}
	}
}
