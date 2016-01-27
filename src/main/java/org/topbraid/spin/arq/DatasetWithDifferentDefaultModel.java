package org.topbraid.spin.arq;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;

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
