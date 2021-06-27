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
package org.topbraid.shacl.rules;

import java.net.URI;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.topbraid.jenax.progress.ProgressMonitor;
import org.topbraid.jenax.util.DatasetWithDifferentDefaultModel;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.entailment.SHACLEntailment;

public class RulesEntailment implements SHACLEntailment.Engine {

	@Override
	public Model createModelWithEntailment(Dataset dataset, URI shapesGraphURI, ShapesGraph shapesGraph, ProgressMonitor monitor) throws InterruptedException {
		Model dataModel = dataset.getDefaultModel();
		Model inferencesModel = JenaUtil.createDefaultModel();
		MultiUnion unionGraph = new MultiUnion(new Graph[] {
			dataModel.getGraph(),
			inferencesModel.getGraph()
		});
		Model unionDataModel = ModelFactory.createModelForGraph(unionGraph);
		Dataset newDataset = new DatasetWithDifferentDefaultModel(unionDataModel, dataset);
		RuleEngine engine = new RuleEngine(newDataset, shapesGraphURI, shapesGraph, inferencesModel);
		engine.setProgressMonitor(monitor);
		engine.executeAll();
		engine.executeAllDefaultValues();
		if(inferencesModel.isEmpty()) {
			return dataModel;
		}
		else {
			return unionDataModel;
		}
	}
}
