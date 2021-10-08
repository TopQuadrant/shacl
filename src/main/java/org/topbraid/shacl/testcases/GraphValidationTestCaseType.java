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
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.engine.ShapesGraphFactory;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.validation.ValidationEngineConfiguration;
import org.topbraid.shacl.validation.ValidationEngineFactory;
import org.topbraid.shacl.validation.ValidationSuggestionGenerator;
import org.topbraid.shacl.validation.ValidationSuggestionGeneratorFactory;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

public class GraphValidationTestCaseType extends TestCaseType {

	public final static List<Property> IGNORED_PROPERTIES = Arrays.asList(new Property[] {
		SH.message, // For TopBraid's suggestions
		SH.resultMessage,
		DASH.suggestionGroup
	});
	
	
	public static void addStatements(Model model, Statement s) {
		if(!IGNORED_PROPERTIES.contains(s.getPredicate())) {
			model.add(s);
		}
		if(s.getObject().isAnon()) {
			for(Statement t : s.getModel().listStatements(s.getResource(), null, (RDFNode)null).toList()) {
				addStatements(model, t);
			}
		}
	}

	
	public static void addSuggestions(Model dataModel, Model shapesModel, Model actualResults) {
		ValidationSuggestionGenerator generator = ValidationSuggestionGeneratorFactory.get().createValidationSuggestionGenerator(dataModel, shapesModel);
		if(generator == null) {
			throw new UnsupportedOperationException("Cannot run test due to no suggestion generator installed");
		}
		generator.addSuggestions(actualResults, Integer.MAX_VALUE, null);
	}


	public GraphValidationTestCaseType() {
		super(DASH.GraphValidationTestCase);
	}


	@Override
	protected TestCase createTestCase(Resource graph, Resource resource) {
		return new GraphValidationTestCase(graph, resource);
	}


	private static class GraphValidationTestCase extends TestCase {
		
		GraphValidationTestCase(Resource graph, Resource resource) {
			super(graph, resource);
		}

		@Override
		public void run(Model results) throws Exception {
			
			Model dataModel = getResource().getModel();

			Dataset dataset = ARQFactory.get().getDataset(dataModel);
			URI shapesGraphURI = SHACLUtil.withShapesGraph(dataset);

			ShapesGraph shapesGraph = ShapesGraphFactory.get().createShapesGraph(dataset.getNamedModel(shapesGraphURI.toString()));

			ValidationEngineConfiguration configuration = new ValidationEngineConfiguration();
			if(!getResource().hasProperty(DASH.validateShapes, JenaDatatypes.TRUE)) {
				configuration.setValidateShapes(false);
			}
			ValidationEngine validationEngine = ValidationEngineFactory.get().create(dataset, shapesGraphURI, shapesGraph, null);
			validationEngine.setConfiguration(configuration);
			validationEngine.applyEntailments();
			Resource actualReport = validationEngine.validateAll();
			Model actualResults = actualReport.getModel();
			actualResults.clearNsPrefixMap();
			actualResults.setNsPrefixes(getResource().getModel());
			if(getResource().hasProperty(DASH.includeSuggestions, JenaDatatypes.TRUE)) {
				Model shapesModel = dataset.getNamedModel(shapesGraphURI.toString());
				addSuggestions(dataModel, shapesModel, actualResults);
			}
			actualResults.setNsPrefix(SH.PREFIX, SH.NS);
			actualResults.setNsPrefix("rdf", RDF.getURI());
			actualResults.setNsPrefix("rdfs", RDFS.getURI());
			for(Property ignoredProperty : IGNORED_PROPERTIES) {
				actualResults.removeAll(null, ignoredProperty, (RDFNode)null);
			}
			Resource expectedReport = getExpectedReport();
			Model expectedModel = JenaUtil.createDefaultModel();
			for(Statement s : expectedReport.listProperties().toList()) {
				expectedModel.add(s);
			}
			for(Statement s : expectedReport.listProperties(SH.result).toList()) {
				for(Statement t : s.getResource().listProperties().toList()) {
					if(t.getPredicate().equals(DASH.suggestion)) {
						addStatements(expectedModel, t);
					}
					else if(SH.resultPath.equals(t.getPredicate())) {
						expectedModel.add(t.getSubject(), t.getPredicate(),
								SHACLPaths.clonePath(t.getResource(), expectedModel));
					}
					else {
						expectedModel.add(t);
						if(SH.detail.equals(t.getPredicate())) {
							for(Statement n : t.getResource().listProperties().toList()) {
								expectedModel.add(n);
							}
						}
					}
				}
			}
			if(expectedModel.getGraph().isIsomorphicWith(actualResults.getGraph())) {
				createResult(results, DASH.SuccessTestCaseResult);
			}
			else {
				expectedModel.setNsPrefixes(actualReport.getModel());
				System.out.println("Expected: " + ModelPrinter.get().print(expectedModel) + "\nActual: " + ModelPrinter.get().print(actualResults));
				Resource failure = createFailure(results, 
						"Mismatching validation results. Expected " + expectedModel.size() + " triples, found " + actualResults.size(),
						ResourceFactory.createStringLiteral(ModelPrinter.get().print(actualResults)));
				failure.addProperty(DASH.expectedResult, ModelPrinter.get().print(expectedModel));
			}
		}

		private Resource getExpectedReport() {
			Statement s = getResource().getProperty(DASH.expectedResult);
			if(s.getObject().isResource()) {
				return s.getResource();
			}
			else {
				Model model = JenaUtil.createMemoryModel();
				model.read(new ByteArrayInputStream(s.getString().getBytes()), "urn:dummy", FileUtils.langTurtle);
				return model.listSubjectsWithProperty(RDF.type, SH.ValidationReport).next();
			}
		}
	}
}
