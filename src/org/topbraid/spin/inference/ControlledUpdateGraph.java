package org.topbraid.spin.inference;

import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEventManager;
import com.hp.hpl.jena.graph.GraphStatisticsHandler;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphWithPerform;
import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler;
import com.hp.hpl.jena.shared.AddDeniedException;
import com.hp.hpl.jena.shared.DeleteDeniedException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;


/**
 * A Graph implementation that is used by SPIN inferencing to
 * support UPDATE rules.
 * The Graph wraps another delegate Graph, and delegates most of
 * its operations to that.
 * However, it records which of the triples have actually been
 * added or deleted - the usual Graph policy is to perform those
 * operations regardless of whether a triple was already there.
 * This makes it possible to determine whether further iterations
 * are needed, and which new rdf:type triples have been added. 
 * 
 * @author Holger Knublauch
 */
class ControlledUpdateGraph implements GraphWithPerform {
	
	private BulkUpdateHandler buh;

	private Graph delegate;
	
	private Set<Triple> addedTriples = new HashSet<Triple>();
	
	private Set<Triple> deletedTriples = new HashSet<Triple>();
	
	
	ControlledUpdateGraph(Graph delegate) {
		this.delegate = delegate;
		this.buh = new SimpleBulkUpdateHandler(this);
	}

	
	@Override
	public void add(Triple t) throws AddDeniedException {
		performAdd(t);
	}

	
	@Override
	public void clear() {
		for(Triple triple : find(Node.ANY, Node.ANY, Node.ANY).toList()) {
			delete(triple);
		}
	}


	@Override
	public boolean dependsOn(Graph other) {
		return delegate.dependsOn(other);
	}

	@Override
	public TransactionHandler getTransactionHandler() {
		return delegate.getTransactionHandler();
	}

	@Override
	public BulkUpdateHandler getBulkUpdateHandler() {
		return buh;
	}

	@Override
	public Capabilities getCapabilities() {
		return delegate.getCapabilities();
	}

	@Override
	public GraphEventManager getEventManager() {
		return delegate.getEventManager();
	}

	@Override
	public GraphStatisticsHandler getStatisticsHandler() {
		return delegate.getStatisticsHandler();
	}

	@Override
	public PrefixMapping getPrefixMapping() {
		return delegate.getPrefixMapping();
	}

	@Override
	public void delete(Triple t) throws DeleteDeniedException {
		performDelete(t);
	}

	@Override
	public ExtendedIterator<Triple> find(TripleMatch m) {
		return delegate.find(m);
	}

	@Override
	public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
		return delegate.find(s, p, o);
	}

	@Override
	public boolean isIsomorphicWith(Graph g) {
		return delegate.isIsomorphicWith(g);
	}

	@Override
	public boolean contains(Node s, Node p, Node o) {
		return delegate.contains(s, p, o);
	}

	@Override
	public boolean contains(Triple t) {
		return delegate.contains(t);
	}

	@Override
	public void close() {
		delegate.close();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isClosed() {
		return delegate.isClosed();
	}


	@Override
	public void performAdd(Triple t) {
		if(!delegate.contains(t)) {
			addedTriples.add(t);
		}
		delegate.add(t);
	}


	@Override
	public void performDelete(Triple t) {
		if(delegate.contains(t)) {
			deletedTriples.add(t);
		}
		delegate.delete(t);
	}
	
	
	@Override
	public void remove(Node s, Node p, Node o) {
		for(Triple triple : find(s, p, o).toList()) {
			delete(triple);
		}
	}


	public Iterable<Triple> getAddedTriples() {
		return addedTriples;
	}
	
	
	public boolean isChanged() {
		return !addedTriples.isEmpty() || !deletedTriples.isEmpty(); 
	}
}
