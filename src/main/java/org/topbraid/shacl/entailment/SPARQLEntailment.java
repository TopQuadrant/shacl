package org.topbraid.shacl.entailment;

import java.util.HashMap;
import java.util.Map;

import org.topbraid.spin.arq.DatasetWithDifferentDefaultModel;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Singleton to support sh:entailment.
 * Extensions may install their own Engines.
 * 
 * TODO: This is currently unused and needs to be re-wired with the rest of the engine.
 * 
 * @author Holger Knublauch
 */
public class SPARQLEntailment {
	
	public final static Resource RDFS = ResourceFactory.createResource("http://www.w3.org/ns/entailment/RDFS");
	
	public static interface Engine {
		
		Model createModelWithEntailment(Model model);
	}

	private static SPARQLEntailment singleton = new SPARQLEntailment();
	
	public static SPARQLEntailment get() {
		return singleton;
	}
	
	private Map<String,Engine> engines = new HashMap<String,Engine>();
	
	
	protected SPARQLEntailment() {
		setEngine(RDFS.getURI(), new Engine() {
			@Override
			public Model createModelWithEntailment(Model model) {
				return ModelFactory.createRDFSModel(model);
			}
		});
	}

	
	public Engine getEngine(String uri) {
		return engines.get(uri);
	}
	
	
	public void setEngine(String uri, Engine engine) {
		engines.put(uri, engine);
	}
	
	
	public Dataset withEntailment(Dataset dataset, Resource entailment) {
		if(entailment == null || dataset.getDefaultModel() == null) {
			return dataset;
		}
		else {
			Engine engine = getEngine(entailment.getURI());
			if(engine != null) {
				Model newDefaultModel = engine.createModelWithEntailment(dataset.getDefaultModel());
				return new DatasetWithDifferentDefaultModel(newDefaultModel, dataset);
			}
			else {
				return null;
			}
		}
	}
}
