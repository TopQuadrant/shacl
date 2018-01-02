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

import java.util.Iterator;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.LabelExistsException;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;

/**
 * A Dataset that simply delegates all its calls, allowing to wrap an existing
 * Dataset (e.g. the TopBraid Dataset).
 * 
 * @author Holger Knublauch
 */
public abstract class DelegatingDataset implements Dataset {

	private Dataset delegate;
	
	public DelegatingDataset(Dataset delegate) {
		this.delegate = delegate;
	}

	@Override
	public DatasetGraph asDatasetGraph() {
		return new DatasetWrappingDatasetGraph(this);
	}

	
	@Override
	public void close() {
		delegate.close();
	}

	
	@Override
	public boolean containsNamedModel(String uri) {
		return delegate.containsNamedModel(uri);
	}

	
	@Override
	public Model getDefaultModel() {
		return delegate.getDefaultModel();
	}

    @Override
    public Model getUnionModel() {
        return delegate.getUnionModel();
    }   

    public Dataset getDelegate() {
		return delegate;
	}

	
	@Override
	public Lock getLock() {
		return delegate.getLock();
	}

	
	@Override
	public Model getNamedModel(String uri) {
		return delegate.getNamedModel(uri);
	}

	
	@Override
	public Iterator<String> listNames() {
		return delegate.listNames();
	}

	
	@Override
	public void setDefaultModel(Model model) {
		delegate.setDefaultModel(model);
	}

	
	@Override
	public void addNamedModel(String uri, Model model)
			throws LabelExistsException {
		delegate.addNamedModel(uri, model);
	}

	
	@Override
	public void removeNamedModel(String uri) {
		delegate.removeNamedModel(uri);
	}

	
	@Override
	public void replaceNamedModel(String uri, Model model) {
		delegate.replaceNamedModel(uri, model);
	}

	
	@Override
	public Context getContext() {
		return delegate.getContext();
	}

	
	@Override
	public boolean supportsTransactions() {
		return delegate.supportsTransactions();
	}

	
	@Override
	public boolean supportsTransactionAbort() {
		return delegate.supportsTransactionAbort();
	}

	@Override
	public void begin(ReadWrite readWrite) {
		delegate.begin(readWrite);
	}

	
	@Override
	public void commit() {
		delegate.commit();
	}

	
	@Override
	public void abort() {
		delegate.abort();
	}

	
	@Override
	public boolean isInTransaction() {
		return delegate.isInTransaction();
	}

	
	@Override
	public void end() {
		delegate.end();
	}
}
