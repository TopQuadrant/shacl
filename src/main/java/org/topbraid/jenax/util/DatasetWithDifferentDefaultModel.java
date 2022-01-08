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
package org.topbraid.jenax.util;

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


    @Override
    public boolean isEmpty() {
        // Don't risk looping on all named graphs.
        throw new UnsupportedOperationException();
    }


	@Override
	public Dataset setDefaultModel(Model model) {
		this.defaultModel = model;
		return this;
	}
}
