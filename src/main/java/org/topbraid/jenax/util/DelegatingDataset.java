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
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.Lock;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;

/**
 * A Dataset that simply delegates all its calls, allowing to wrap an existing
 * Dataset (e.g. the TopBraid Dataset).
 * 
 * @author Holger Knublauch
 */
public class DelegatingDataset implements Dataset {

	private Dataset delegate;

	
	public DelegatingDataset(Dataset delegate) {
		this.delegate = delegate;
	}


	@Override
	public Dataset addNamedModel(Resource resource, Model model) {
		delegate.addNamedModel(resource, model);
		return this;
	}

	
	@Override
	public Dataset addNamedModel(String uri, Model model) {
		delegate.addNamedModel(uri, model);
		return this;
	}

	
	@Override
	public void abort() {
		delegate.abort();
	}

	
	@Override
	public DatasetGraph asDatasetGraph() {
		return new DatasetWrappingDatasetGraph(this);
	}

	
    @Override
    public void begin(TxnType type) {
        delegate.begin(type);
    }

    
	@Override
	public void begin(ReadWrite readWrite) {
		delegate.begin(readWrite);
	}

	
	@Override
	public void close() {
		delegate.close();
	}

	
    @Override
	public void commit() {
		delegate.commit();
	}


	@Override
	public boolean containsNamedModel(Resource resource) {
		return delegate.containsNamedModel(resource);
	}

	
	@Override
	public boolean containsNamedModel(String uri) {
		return delegate.containsNamedModel(uri);
	}

	
	@Override
	public void end() {
		delegate.end();
	}

	
	@Override
	public Context getContext() {
		return delegate.getContext();
	}

	
	@Override
	public Model getDefaultModel() {
		return delegate.getDefaultModel();
	}

    
    public Dataset getDelegate() {
		return delegate;
	}

	
	@Override
	public Lock getLock() {
		return delegate.getLock();
	}


	@Override
	public Model getNamedModel(Resource resource) {
        return delegate.getNamedModel(resource);
	}

	
	@Override
	public Model getNamedModel(String uri) {
		return delegate.getNamedModel(uri);
	}

	
    @Override
    public Model getUnionModel() {
        return delegate.getUnionModel();
    }   

    
    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

	
	@Override
	public boolean isInTransaction() {
		return delegate.isInTransaction();
	}


	@Override
	public Iterator<Resource> listModelNames() {
		return delegate.listModelNames();
	}

	
	@Override
	public Iterator<String> listNames() {
		return delegate.listNames();
	}

	
    @Override
    public boolean promote(Promote mode) {
        return delegate.promote(mode);
    }


	@Override
	public Dataset removeNamedModel(Resource resource) {
		delegate.removeNamedModel(resource);
		return this;
	}

	
	@Override
	public Dataset removeNamedModel(String uri) {
		delegate.removeNamedModel(uri);
        return this;
	}


	@Override
	public Dataset replaceNamedModel(Resource resource, Model model) {
		delegate.replaceNamedModel(resource, model);
		return this;
	}

	
	@Override
	public Dataset replaceNamedModel(String uri, Model model) {
		delegate.replaceNamedModel(uri, model);
        return this;
	}

	
	@Override
	public Dataset setDefaultModel(Model model) {
		delegate.setDefaultModel(model);
		return this;
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
    public TxnType transactionType() {
        return delegate.transactionType();
    }

    
    @Override
    public ReadWrite transactionMode() {
        return delegate.transactionMode();
    }
}
