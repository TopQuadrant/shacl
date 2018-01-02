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

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.engine.filters.ExcludeMetaShapesFilter;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.validation.ValidationSuggestionGenerator;
import org.topbraid.shacl.validation.ValidationSuggestionGeneratorFactory;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.validation.ValidationEngineFactory;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

public class GraphValidationTestCaseType implements TestCaseType {

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


	@Override
	public Collection<TestCase> getTestCases(Model model, Resource graph) {
		List<TestCase> results = new LinkedList<TestCase>();
		for(Resource resource : JenaUtil.getAllInstances(model.getResource(DASH.GraphValidationTestCase.getURI()))) {
			results.add(new GraphValidationTestCase(graph, resource));
		}
		return results;
	}

	
	private static class GraphValidationTestCase extends TestCase {
		
		GraphValidationTestCase(Resource graph, Resource resource) {
			super(graph, resource);
		}

		@Override
		public void run(Model results) throws Exception {
			
			Model dataModel = SHACLUtil.withDefaultValueTypeInferences(getResource().getModel());

			Dataset dataset = ARQFactory.get().getDataset(dataModel);
			URI shapesGraphURI = SHACLUtil.withShapesGraph(dataset);

			ShapesGraph shapesGraph = new ShapesGraph(dataset.getNamedModel(shapesGraphURI.toString()));
			if(!getResource().hasProperty(DASH.validateShapes, JenaDatatypes.TRUE)) {
				shapesGraph.setShapeFilter(new ExcludeMetaShapesFilter());
			}
			ValidationEngine validationEngine = ValidationEngineFactory.get().create(dataset, shapesGraphURI, shapesGraph, null);
			validationEngine.applyEntailments();
			Resource actualReport = validationEngine.validateAll();
			Model actualResults = actualReport.getModel();
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
			Model expectedModel = JenaUtil.createDefaultModel();
			Resource expectedReport = getResource().getPropertyResourceValue(DASH.expectedResult);
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
					}
				}
			}
			if(expectedModel.getGraph().isIsomorphicWith(actualResults.getGraph())) {
				createResult(results, DASH.SuccessTestCaseResult);
			}
			else {
				createFailure(results, 
						"Mismatching validation results. Expected " + expectedModel.size() + " triples, found " + actualResults.size());
			}
		}
	}
}
