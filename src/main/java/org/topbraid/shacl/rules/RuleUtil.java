package org.topbraid.shacl.rules;

import java.net.URI;
import java.util.UUID;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.shacl.arq.SHACLFunctions;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.util.SHACLSystemModel;
import org.topbraid.shacl.vocabulary.TOSH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.progress.ProgressMonitor;
import org.topbraid.spin.util.JenaUtil;

/**
 * Convenience methods to execute SHACL rules.
 * 
 * @author Holger Knublauch
 */
public class RuleUtil {

	/**
	 * Executes all rules from a given shapes Model on a given data Model.
	 * If the shapesModel does not include the system graph triples then these will be added.
	 * If inferencesModel is not null then it must be part of the dataModel (e.g. a sub-graph)
	 * of a Jena MultiUnion object.
	 * Otherwise, the function will create a new inferences Model which is merged with the
	 * dataModel for the duration of the execution.
	 * @param dataModel  the data Model
	 * @param shapesModel  the shapes Model
	 * @param inferencesModel  the Model for the inferred triples or null
	 * @param monitor  an optional progress monitor
	 * @return the Model of inferred triples (i.e. inferencesModel if not null, or a new Model)
	 */
	public static Model executeRules(Model dataModel, Model shapesModel, Model inferencesModel, ProgressMonitor monitor) {
		
		// Ensure that the SHACL, DASH and TOSH graphs are present in the shapes Model
		if(!shapesModel.contains(TOSH.hasShape, RDF.type, (RDFNode)null)) { // Heuristic
			Model unionModel = SHACLSystemModel.getSHACLModel();
			MultiUnion unionGraph = new MultiUnion(new Graph[] {
				unionModel.getGraph(),
				shapesModel.getGraph()
			});
			shapesModel = ModelFactory.createModelForGraph(unionGraph);
		}

		// Make sure all sh:Functions are registered
		SHACLFunctions.registerFunctions(shapesModel);
		
		if(inferencesModel == null) {
			inferencesModel = JenaUtil.createDefaultModel();
			MultiUnion unionGraph = new MultiUnion(new Graph[] {
				dataModel.getGraph(),
				inferencesModel.getGraph()
			});
			dataModel = ModelFactory.createModelForGraph(unionGraph);
		}
		
		// Create Dataset that contains both the data model and the shapes model
		// (here, using a temporary URI for the shapes graph)
		URI shapesGraphURI = URI.create("urn:x-shacl-shapes-graph:" + UUID.randomUUID().toString());
		Dataset dataset = ARQFactory.get().getDataset(dataModel);
		dataset.addNamedModel(shapesGraphURI.toString(), shapesModel);

		ShapesGraph shapesGraph = new ShapesGraph(shapesModel);
		RuleEngine engine = new RuleEngine(dataset, shapesGraphURI, shapesGraph, inferencesModel);
		engine.setProgressMonitor(monitor);
		
		try {
			engine.executeAll();
		}
		catch(InterruptedException ex) {
		}
		return inferencesModel;
	}
}
