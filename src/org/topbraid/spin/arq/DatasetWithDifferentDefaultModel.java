package org.topbraid.spin.arq;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * A DelegatingDataset that uses a different default model than the delegate.
 * 
 * @author Holger Knublauch
 */
public class DatasetWithDifferentDefaultModel extends DelegatingDataset {

	private Model defaultModel;
	
	
	public DatasetWithDifferentDefaultModel(Model defaultModel, Dataset delegate) {
		super(delegate);
		this.defaultModel = defaultModel;
	}

	
	@Override
	public Model getDefaultModel() {
		return defaultModel;
	}	
}
