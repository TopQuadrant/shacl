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

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.vocabulary.DOAP;
import org.apache.jena.sparql.vocabulary.EARL;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.ExceptionUtil;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.engine.ShapesGraphFactory;
import org.topbraid.shacl.engine.filters.CoreConstraintFilter;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.validation.ValidationEngineConfiguration;
import org.topbraid.shacl.validation.ValidationEngineFactory;
import org.topbraid.shacl.vocabulary.*;

import java.io.*;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Helper object for executing the W3C test cases for SHACL.
 * The tests are assumed to be in a folder structure mirroring
 * <p>
 * https://github.com/w3c/data-shapes/tree/gh-pages/data-shapes-test-suite/tests
 *
 * @author Holger Knublauch
 */
public class W3CTestRunner {

    private final static Resource EARL_AUTHOR = ResourceFactory.createResource("http://knublauch.com");

    private final static Resource EARL_SUBJECT = ResourceFactory.createResource("http://topquadrant.com/shacl/api");

    private Model earl;

    private List<Item> items = new LinkedList<>();


    public W3CTestRunner(File rootManifest) throws IOException {

        earl = JenaUtil.createMemoryModel();
        JenaUtil.initNamespaces(earl.getGraph());
        earl.setNsPrefix("doap", DOAP.NS);
        earl.setNsPrefix("earl", EARL.NS);

        earl.add(EARL_SUBJECT, RDF.type, DOAP.Project);
        earl.add(EARL_SUBJECT, RDF.type, EARL.Software);
        earl.add(EARL_SUBJECT, RDF.type, EARL.TestSubject);
        earl.add(EARL_SUBJECT, DOAP.developer, EARL_AUTHOR);
        earl.add(EARL_SUBJECT, DOAP.name, "TopBraid SHACL API");

        collectItems(rootManifest, "urn:x:root/");
    }


    private void collectItems(File manifestFile, String baseURI) throws IOException {

        String filePath = manifestFile.getAbsolutePath().replaceAll("\\\\", "/");
        int coreIndex = filePath.indexOf("core/");
        if (coreIndex > 0 && !filePath.contains("sparql/core")) {
            filePath = filePath.substring(coreIndex);
        } else {
            int sindex = filePath.indexOf("sparql/");
            if (sindex > 0) {
                filePath = filePath.substring(sindex);
            }
        }

        Model model = JenaUtil.createMemoryModel();
        model.read(new FileInputStream(manifestFile), baseURI, FileUtils.langTurtle);

        for (Resource manifest : model.listSubjectsWithProperty(RDF.type, MF.Manifest).toList()) {
            for (Resource include : JenaUtil.getResourceProperties(manifest, MF.include)) {
                String path = include.getURI().substring(baseURI.length());
                File includeFile = new File(manifestFile.getParentFile(), path);
                if (path.contains("/")) {
                    String addURI = path.substring(0, path.indexOf('/'));
                    collectItems(includeFile, baseURI + addURI + "/");
                } else {
                    collectItems(includeFile, baseURI + path);
                }
            }
            for (Resource entries : JenaUtil.getResourceProperties(manifest, MF.entries)) {
                for (RDFNode entry : entries.as(RDFList.class).iterator().toList()) {
                    items.add(new Item(entry.asResource(), filePath, manifestFile));
                }
            }
        }
    }


    public Model getEARLModel() {
        return earl;
    }


    public List<Item> getItems() {
        return items;
    }


    public void run(PrintStream out) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        out.println("Running " + items.size() + " W3C Test Cases...");
        int count = 0;
        for (Item item : items) {
            if (!item.run(out)) {
                count++;
            }
        }
        out.println("Completed: " + count + " test failures (Duration: " + (System.currentTimeMillis() - startTime) + " ms)");
    }


    public class Item {

        // The sht:Validate in its defining Model
        Resource entry;

        String filePath;

        File manifestFile;


        Item(Resource entry, String filePath, File manifestFile) {
            this.entry = entry;
            this.filePath = filePath;
            this.manifestFile = manifestFile;
        }


        public Resource getEARLResource() {
            return ResourceFactory.createResource("urn:x-shacl-test:" + entry.getURI().substring("urn:x:root".length()));
        }


        public String getFilePath() {
            return filePath;
        }


        public String getLabel() {
            return JenaUtil.getStringProperty(entry, RDFS.label);
        }


        public Resource getStatus() {
            return entry.getPropertyResourceValue(MF.status);
        }


        public boolean run(PrintStream out) throws InterruptedException {

            Resource assertion = earl.createResource(EARL.Assertion);
            assertion.addProperty(EARL.assertedBy, EARL_AUTHOR);
            assertion.addProperty(EARL.subject, EARL_SUBJECT);
            assertion.addProperty(EARL.test, getEARLResource());
            Resource result = earl.createResource(EARL.TestResult);
            assertion.addProperty(EARL.result, result);
            result.addProperty(EARL.mode, EARL.automatic);

            Resource action = entry.getPropertyResourceValue(MF.action);
            Resource shapesGraphResource = action.getPropertyResourceValue(SHT.shapesGraph);
            Graph shapesBaseGraph = entry.getModel().getGraph();
            if (!(entry.getURI() + ".ttl").equals(shapesGraphResource.getURI())) {
                int last = shapesGraphResource.getURI().lastIndexOf('/');
                File shapesFile = new File(manifestFile.getParentFile(), shapesGraphResource.getURI().substring(last + 1));
                Model shapesModel = JenaUtil.createMemoryModel();
                try {
                    shapesModel.read(new FileInputStream(shapesFile), "urn:x:dummy", FileUtils.langTurtle);
                    shapesBaseGraph = shapesModel.getGraph();
                } catch (FileNotFoundException e) {
                    ExceptionUtil.throwUnchecked(e);
                }
            }

            MultiUnion multiUnion = new MultiUnion(new Graph[]{
                    shapesBaseGraph,
                    ARQFactory.getNamedModel(TOSH.BASE_URI).getGraph(),
                    ARQFactory.getNamedModel(DASH.BASE_URI).getGraph(),
                    ARQFactory.getNamedModel(SH.BASE_URI).getGraph()
            });
            Model shapesModel = ModelFactory.createModelForGraph(multiUnion);

            Model dataModel = entry.getModel();
            Resource dataGraph = action.getPropertyResourceValue(SHT.dataGraph);
            if (!(entry.getURI() + ".ttl").equals(dataGraph.getURI())) {
                int last = dataGraph.getURI().lastIndexOf('/');
                File dataFile = new File(manifestFile.getParentFile(), dataGraph.getURI().substring(last + 1));
                dataModel = JenaUtil.createMemoryModel();
                try {
                    dataModel.read(new FileInputStream(dataFile), "urn:x:dummy", FileUtils.langTurtle);
                } catch (FileNotFoundException e) {
                    ExceptionUtil.throwUnchecked(e);
                }
            }

            URI shapesGraphURI = URI.create("urn:x-shacl-shapes-graph:" + UUID.randomUUID());
            Dataset dataset = ARQFactory.get().getDataset(dataModel);
            dataset.addNamedModel(shapesGraphURI.toString(), shapesModel);

            ShapesGraph shapesGraph = ShapesGraphFactory.get().createShapesGraph(shapesModel);
            ValidationEngineConfiguration configuration = new ValidationEngineConfiguration().setValidateShapes(false);
            if (entry.hasProperty(ResourceFactory.createProperty(MF.NS + "requires"), SHT.CoreOnly)) {
                shapesGraph.setConstraintFilter(new CoreConstraintFilter());
            }
            ValidationEngine engine = ValidationEngineFactory.get().create(dataset, shapesGraphURI, shapesGraph, null);
            engine.setConfiguration(configuration);
            try {
                Resource actualReport = engine.validateAll();
                Model actualResults = actualReport.getModel();
                actualResults.setNsPrefix(SH.PREFIX, SH.NS);
                actualResults.setNsPrefix("rdf", RDF.getURI());
                actualResults.setNsPrefix("rdfs", RDFS.getURI());
                Model expectedModel = JenaUtil.createDefaultModel();
                Resource expectedReport = entry.getPropertyResourceValue(MF.result);
                for (Statement s : expectedReport.listProperties().toList()) {
                    expectedModel.add(s);
                }
                for (Statement s : expectedReport.listProperties(SH.result).toList()) {
                    for (Statement t : s.getResource().listProperties().toList()) {
                        if (t.getPredicate().equals(DASH.suggestion)) {
                            GraphValidationTestCaseType.addStatements(expectedModel, t);
                        } else if (SH.resultPath.equals(t.getPredicate())) {
                            expectedModel.add(t.getSubject(), t.getPredicate(),
                                    SHACLPaths.clonePath(t.getResource(), expectedModel));
                        } else {
                            expectedModel.add(t);
                        }
                    }
                }
                actualResults.removeAll(null, SH.message, (RDFNode) null);
                for (Statement s : actualResults.listStatements(null, SH.resultMessage, (RDFNode) null).toList()) {
                    if (!expectedModel.contains(null, SH.resultMessage, s.getObject())) {
                        actualResults.remove(s);
                    }
                }
                if (expectedModel.getGraph().isIsomorphicWith(actualResults.getGraph())) {
                    out.println("PASSED: " + entry);
                    result.addProperty(EARL.outcome, EARL.passed);
                    return true;
                } else {
                    out.println("FAILED: " + entry);
                    result.addProperty(EARL.outcome, EARL.failed);
                    expectedModel.setNsPrefixes(actualResults);
                    System.out.println("Expected\n" + ModelPrinter.get().print(expectedModel));
                    System.out.println("Actual\n" + ModelPrinter.get().print(actualResults));
                    return false;
                }
            } catch (Exception ex) {
                if (entry.hasProperty(MF.result, SHT.Failure)) {
                    out.println("PASSED: " + entry);
                    result.addProperty(EARL.outcome, EARL.passed);
                    return true;
                } else {
                    out.println("EXCEPTION: " + entry + " " + ex.getMessage());
                    result.addProperty(EARL.outcome, EARL.failed);
                    return false;
                }
            }
        }
    }
}
