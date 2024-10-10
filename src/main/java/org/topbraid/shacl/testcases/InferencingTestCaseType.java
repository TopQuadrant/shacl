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

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.jenax.functions.CurrentThreadFunctionRegistry;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.engine.ShapesGraphFactory;
import org.topbraid.shacl.rules.RuleEngine;
import org.topbraid.shacl.testcases.context.TestCaseContextFactory;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.DASH;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ashley Caselli
 */
public class InferencingTestCaseType extends TestCaseType {

    private static final List<TestCaseContextFactory> contextFactories = new LinkedList<>();


    public static void registerContextFactory(TestCaseContextFactory factory) {
        contextFactories.add(factory);
    }


    public InferencingTestCaseType() {
        super(DASH.InferencingTestCase);
    }


    @Override
    protected TestCase createTestCase(Resource graph, Resource resource) {
        return new InferencingTestCase(graph, resource);
    }


    private static class InferencingTestCase extends TestCase {

        InferencingTestCase(Resource graph, Resource resource) {
            super(graph, resource);
        }


        @Override
        public void run(Model results) {
            Resource testCase = getResource();

            Runnable tearDownCTFR = CurrentThreadFunctionRegistry.register(testCase.getModel());

            Model dataModel = getResource().getModel();

            Dataset dataset = ARQFactory.get().getDataset(dataModel);
            URI shapesGraphURI = SHACLUtil.withShapesGraph(dataset);
            ShapesGraph shapesGraph = ShapesGraphFactory.get().createShapesGraph(dataset.getNamedModel(shapesGraphURI.toString()));

            RuleEngine ruleEngine = new RuleEngine(dataset, shapesGraphURI, shapesGraph, dataModel);
            try {
                ruleEngine.executeAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                tearDownCTFR.run();
            }

            Model actualResults = ruleEngine.getInferencesModel();
            Model expectedModel = JenaUtil.createDefaultModel();

            List<Statement> expectedResults = getResource().listProperties(DASH.expectedResult).toList();
            boolean valid = false;
            for (Statement s : expectedResults) {
                valid = false;
                expectedModel.add(s);
                Resource expectedInferredNode = s.getObject().asResource();
                Triple expectedInferredNodeAsTriple = Triple.create(expectedInferredNode.getProperty(RDF.subject).getObject().asNode(),
                        expectedInferredNode.getProperty(RDF.predicate).getObject().asNode(),
                        expectedInferredNode.getProperty(RDF.object).getObject().asNode());
                if (actualResults.getGraph().contains(expectedInferredNodeAsTriple)) {
                    valid = true;
                }
            }

            if (valid) {
                createResult(results, DASH.SuccessTestCaseResult);
            } else {
                System.out.println("Expected: " + ModelPrinter.get().print(expectedModel) + "\nActual: " + ModelPrinter.get().print(actualResults));
                Resource failure = createFailure(results,
                        "Mismatching inference results. Expected " + expectedModel.size() + " triples, found " + actualResults.size());
                failure.addProperty(DASH.expectedResult, ModelPrinter.get().print(expectedModel));
            }
        }

    }
}
