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
package org.topbraid.shacl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.js.JSScriptEngine;
import org.topbraid.shacl.js.JSScriptEngineFactory;
import org.topbraid.shacl.js.NashornScriptEngine;
import org.topbraid.shacl.testcases.TestCase;
import org.topbraid.shacl.testcases.TestCaseType;
import org.topbraid.shacl.testcases.TestCaseTypes;
import org.topbraid.shacl.util.SHACLSystemModel;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

@RunWith(Parameterized.class)
public class TestDASHTestCases {
	
	static {
		// Redirect loading of text JS files to local folder
		JSScriptEngineFactory.set(new JSScriptEngineFactory() {
			@Override
			public JSScriptEngine createScriptEngine() {
				return new NashornScriptEngine() {
					@Override
                    protected Reader createScriptReader(String url) throws Exception {
						if(DASH_JS.equals(url)) {
							return new InputStreamReader(NashornScriptEngine.class.getResourceAsStream("/rdf/dash.js"));
						}
						else if(RDFQUERY_JS.equals(url)) {
							return new InputStreamReader(NashornScriptEngine.class.getResourceAsStream("/rdf/rdfquery.js"));
						}
						else if(url.startsWith("http://datashapes.org/js/")) {
							return new InputStreamReader(NashornScriptEngine.class.getResourceAsStream(url.substring(21)));
						}
						else {
							return new InputStreamReader(new URL(url).openStream());
						}
					}
				};
			}
		});
	}

	@Parameters(name="{0}")
	public static Collection<Object[]> data() throws Exception {

		List<TestCase> testCases = new LinkedList<TestCase>();
		File rootFolder = new File("src/test/resources");
		collectTestCases(rootFolder, testCases);
		
		List<Object[]> results = new LinkedList<Object[]>();
		for(TestCase testCase : testCases) {
			results.add(new Object[]{ testCase });
		}
	    return results;
	}

	
	private static void collectTestCases(File folder, List<TestCase> testCases) throws Exception {
		for(File f : folder.listFiles()) {
			if(f.isDirectory()) {
				collectTestCases(f, testCases);
			}
			else if(f.isFile() && f.getName().endsWith(".ttl")) {
				Model testModel = JenaUtil.createDefaultModel();
				InputStream is = new FileInputStream(f);
				testModel.read(is, "urn:dummy", FileUtils.langTurtle);
				testModel.add(SHACLSystemModel.getSHACLModel());
				Resource ontology = testModel.listStatements(null, OWL.imports, ResourceFactory.createResource(DASH.BASE_URI)).next().getSubject();
				for(TestCaseType type : TestCaseTypes.getTypes()) {
					testCases.addAll(type.getTestCases(testModel, ontology));
				}
			}
		}
	}
	
	
	private TestCase testCase;
	
	public TestDASHTestCases(TestCase testCase) {
		this.testCase = testCase;
	}
	
	
	@Test
	public void testTestCase() {
		System.out.println(" - " + testCase.getResource());
		Model results = JenaUtil.createMemoryModel();
		try {
			testCase.run(results);
		}
		catch(Exception ex) {
			testCase.createFailure(results, "Exception during test case execution: " + ex);
			ex.printStackTrace();
		}
		for(Statement s : results.listStatements(null, RDF.type, DASH.FailureTestCaseResult).toList()) {
			String message = JenaUtil.getStringProperty(s.getSubject(), SH.resultMessage);
			if(message == null) {
				message = "(No " + SH.PREFIX + ":" + SH.resultMessage.getLocalName() + " found in failure)";
			}
			Assert.fail(testCase.getResource() + ": " + message);
		}
	}
}
