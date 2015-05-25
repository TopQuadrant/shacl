package org.topbraid.shacl.arq;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.topbraid.shacl.vocabulary.SH;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.DatasetImpl;

/**
 * A Dataset to be used within SHACL operations.
 * Provides support for the special named graph sh:ShapesGraph.
 * 
 * @author Holger Knublauch
 */
public class SHACLDataset extends DatasetImpl {
	
	private Model shapesModel;
	

	public SHACLDataset(Model defaultModel) {
		super(defaultModel);
		shapesModel = createShapesModel(this);
	}

	
	@Override
	public boolean containsNamedModel(String uri) {
		if(SH.ShapesGraph.getURI().equals(uri)) {
			return true;
		}
		else {
			return super.containsNamedModel(uri);
		}
	}

	
	@Override
	public Model getNamedModel(String uri) {
		if(SH.ShapesGraph.getURI().equals(uri)) {
			return shapesModel;
		}
		else {
			return super.getNamedModel(uri);
		}
	}

	
	/**
	 * Creates a semantics Model for a given input Model.
	 * The semantics Model is the union of the input Model with all graphs referenced via
	 * the sh:shapesGraph property (and transitive includes or semantics of those).
	 * @param model  the Model to create the semantics Model for
	 * @return a Model including the semantics
	 */
	public static Model createShapesModel(Dataset dataset) {
		
		Model model = dataset.getDefaultModel();
		Set<Graph> graphs = new HashSet<Graph>();
		Graph baseGraph = model.getGraph();
		graphs.add(baseGraph);
		
		for(Statement s : model.listStatements(null, SH.shapesGraph, (RDFNode)null).toList()) {
			if(s.getObject().isURIResource()) {
				String graphURI = s.getResource().getURI();
				Model sm = dataset.getNamedModel(graphURI);
				graphs.add(sm.getGraph());
				// TODO: Include includes of sm
			}
		}
		
		if(graphs.size() > 1) {
			MultiUnion union = new MultiUnion(graphs.iterator());
			union.setBaseGraph(baseGraph);
			return ModelFactory.createModelForGraph(union);
		}
		else {
			return model;
		}
	}


	public static Resource withShapesGraph(Dataset dataset) {
		Resource shapesGraph = ResourceFactory.createResource("urn:x-shacl:" + UUID.randomUUID());
		Model shapesModel = SHACLDataset.createShapesModel(dataset);
		dataset.addNamedModel(shapesGraph.getURI(), shapesModel);
		return shapesGraph;
	}
}
