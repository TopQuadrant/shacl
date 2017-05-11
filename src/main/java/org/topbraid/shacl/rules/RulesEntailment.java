package org.topbraid.shacl.rules;

import java.net.URI;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.entailment.SHACLEntailment;
import org.topbraid.spin.progress.ProgressMonitor;
import org.topbraid.spin.util.JenaUtil;

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
		RuleEngine engine = new RuleEngine(dataset, shapesGraphURI, shapesGraph, inferencesModel);
		engine.setProgressMonitor(monitor);
		engine.executeAll();
		if(inferencesModel.isEmpty()) {
			return dataModel;
		}
		else {
			return unionDataModel;
		}
	}
}
