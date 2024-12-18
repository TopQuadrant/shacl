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
package org.topbraid.shacl.entailment;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.topbraid.jenax.progress.ProgressMonitor;
import org.topbraid.jenax.util.DatasetWithDifferentDefaultModel;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.rules.RulesEntailment;
import org.topbraid.shacl.vocabulary.SH;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton to support sh:entailment.
 * Extensions may install their own Engines.
 *
 * @author Holger Knublauch
 */
public class SHACLEntailment {

    public final static Resource RDFS = ResourceFactory.createResource("http://www.w3.org/ns/entailment/RDFS");

    public interface Engine {
        Model createModelWithEntailment(Dataset dataset, URI shapesGraphURI, ShapesGraph shapesGraph, ProgressMonitor monitor) throws InterruptedException;
    }

    private static final SHACLEntailment singleton = new SHACLEntailment();

    public static SHACLEntailment get() {
        return singleton;
    }

    private Map<String, Engine> engines = new HashMap<>();


    protected SHACLEntailment() {
        setEngine(RDFS.getURI(), new Engine() {
            @Override
            public Model createModelWithEntailment(Dataset dataset, URI shapesGraphURI, ShapesGraph shapesGraph, ProgressMonitor monitor) {
                return ModelFactory.createRDFSModel(dataset.getDefaultModel());
            }
        });
        setEngine(SH.Rules.getURI(), new RulesEntailment());
    }


    public Engine getEngine(String uri) {
        return engines.get(uri);
    }


    public void setEngine(String uri, Engine engine) {
        engines.put(uri, engine);
    }


    public Dataset withEntailment(Dataset dataset, URI shapesGraphURI, ShapesGraph shapesGraph, Resource entailment, ProgressMonitor monitor) throws InterruptedException {
        if (entailment == null || dataset.getDefaultModel() == null) {
            return dataset;
        } else {
            Engine engine = getEngine(entailment.getURI());
            if (engine != null) {
                Model newDefaultModel = engine.createModelWithEntailment(dataset, shapesGraphURI, shapesGraph, monitor);
                return new DatasetWithDifferentDefaultModel(newDefaultModel, dataset);
            } else {
                return null;
            }
        }
    }
}
