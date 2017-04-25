package org.topbraid.shacl.entailment;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.topbraid.shacl.rules.RulesEntailment;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.DatasetWithDifferentDefaultModel;
import org.topbraid.spin.progress.ProgressMonitor;

/**
 * Singleton to support sh:entailment.
 * Extensions may install their own Engines.
 * 
 * TODO: This is currently unused and needs to be re-wired with the rest of the engine.
 * 
 * @author Holger Knublauch
 */
public class SHACLEntailment {
	
	public final static Resource RDFS = ResourceFactory.createResource("http://www.w3.org/ns/entailment/RDFS");
	
	public static interface Engine {
		
		Model createModelWithEntailment(Model model, ProgressMonitor monitor) throws InterruptedException;
	}

	private static SHACLEntailment singleton = new SHACLEntailment();
	
	public static SHACLEntailment get() {
		return singleton;
	}
	
	private Map<String,Engine> engines = new HashMap<String,Engine>();
	
	
	protected SHACLEntailment() {
		setEngine(RDFS.getURI(), new Engine() {
			@Override
			public Model createModelWithEntailment(Model model, ProgressMonitor monitor) {
				return ModelFactory.createRDFSModel(model);
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
	
	
	public Dataset withEntailment(Dataset dataset, Resource entailment, ProgressMonitor monitor) throws InterruptedException {
		if(entailment == null || dataset.getDefaultModel() == null) {
			return dataset;
		}
		else {
			Engine engine = getEngine(entailment.getURI());
			if(engine != null) {
				Model newDefaultModel = engine.createModelWithEntailment(dataset.getDefaultModel(), monitor);
				return new DatasetWithDifferentDefaultModel(newDefaultModel, dataset);
			}
			else {
				return null;
			}
		}
	}
}
