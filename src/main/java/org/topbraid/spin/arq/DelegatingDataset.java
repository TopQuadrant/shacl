package org.topbraid.spin.arq;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.LabelExistsException;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphBase;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.util.Context;

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
		return new DatasetGraphBase() {

			@Override
			public void close() {
				DelegatingDataset.this.close();
			}

			@Override
			public boolean containsGraph(Node graphNode) {
				return DelegatingDataset.this.containsNamedModel(graphNode.getURI());
			}

			@Override
			public Graph getDefaultGraph() {
				Model defaultModel = DelegatingDataset.this.getDefaultModel();
				if(defaultModel != null) {
					return defaultModel.getGraph();
				}
				else {
					return null;
				}
			}

			@Override
			public Graph getGraph(Node graphNode) {
				Model model = DelegatingDataset.this.getNamedModel(graphNode.getURI());
				if(model != null) {
					return model.getGraph();
				}
				else {
					return null;
				}
			}

			@Override
			public Lock getLock() {
				return DelegatingDataset.this.getLock();
			}

			@Override
			public Iterator<Node> listGraphNodes() {
				List<Node> results = new LinkedList<Node>();
				Iterator<String> names = DelegatingDataset.this.listNames();
				while(names.hasNext()) {
					String name = names.next();
					results.add(NodeFactory.createURI(name));
				}
				return results.iterator();
			}

			@Override
			public long size() {
				int count = 0;
				Iterator<Node> it = listGraphNodes();
				while(it.hasNext()) {
					it.next();
					count++;
				}
				return count;
			}

			@Override
			public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
				return null;
			}

			@Override
			public Iterator<Quad> findNG(Node g, Node s, Node p, Node o) {
				return null;
			}

			@Override
			public void addGraph(Node arg0, Graph arg1) {
			}

			@Override
			public void removeGraph(Node arg0) {
			}
		};
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
