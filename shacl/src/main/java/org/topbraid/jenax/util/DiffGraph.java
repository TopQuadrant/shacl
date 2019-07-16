package org.topbraid.jenax.util;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphMatcher;
import org.apache.jena.graph.impl.GraphWithPerform;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.util.iterator.ExtendedIterator;


// TODO: note this graph does not correctly implement GraphWithPerform and event notification contracts
/**
 * A WrappedGraph that filters out deleted triples or adds added triples, without
 * modifying the underlying base graph.
 * 
 * Similar to BufferingGraph in TopBraid, but with minimal code dependencies
 * and simpler contracts that are just right for use in SPARQLMotion.
 * 
 * @author Holger Knublauch
 */
public class DiffGraph extends TransparentWrappedGraph {

	/**
	 * This graph has additional triples that are not in the delegate.
	 */
	private GraphWithPerform addedGraph = new GraphMem();
	
	/**
	 * This Set has triples that are in the delegate but are excluded
	 * from the filtered graph.
	 */
	protected Set<Triple> deletedTriples = new HashSet<Triple>();
	
	private PrefixMapping pm;

	
	public DiffGraph(Graph base) {
		super(base);
	}
	
	
	@Override
	public void add(Triple t) {
		performAdd(t);
	}


	@Override
	public void delete(Triple t) {
		performDelete(t);
	}


	public Graph getAddedGraph() {
		return addedGraph;
	}

	
	@Override
	public boolean contains(Node s, Node p, Node o) {
		return contains(Triple.create(s, p, o));
	}
	
	
	@Override
	public boolean contains(Triple t) {
		if(addedGraph.contains(t)) {
			return true;
		}
		else {
			ExtendedIterator<Triple> it = base.find(t);
			while(it.hasNext()) {
				Triple n = it.next();
				if(!deletedTriples.contains(n)) {
					it.close();
					return true;
				}
			}
			return false;
		}
	}

	
	// TODO: If the delegate does not use equals for add and delete
	// but sameValueAs then this code is incorrect.
	// Specifically we should be able to show bugs with TDB which does
	// something different from either equals or sameValueAs.
	private boolean containsByEquals(Graph g,Triple t) {
		ExtendedIterator<Triple> it = g.find(t);
		try {
			while (it.hasNext()) {
				if (t.equals(it.next())) 
					return true;
			}
		}
		finally {
			it.close();
		}
		return false;
	}


	@Override
	public ExtendedIterator<Triple> find(Node s, Node p, Node o) {

		// First get the underlying base query (without any buffered triples)
		ExtendedIterator<Triple> base = super.find(s, p, o);

		// If deleted triples exist then continue with a filtered iterator
		if(deletedTriples.size() > 0) {
			// base without deleted triples.
			base = base.filterDrop(deletedTriples::contains);
		}

		// If added triples exist then chain the two together
		// this iterator supports remove and removes correctly for this graph
		ExtendedIterator<Triple> added = addedGraph.find(s, p, o);
		if(added.hasNext()) {
			return base.andThen(added);  // base and added are guaranteed disjoint
		}
		else {
			return base;
		}
	}


	@Override
	public ExtendedIterator<Triple> find(Triple m) {
		return find(m.getMatchSubject(), m.getMatchPredicate(), m.getMatchObject());
	}


	public Set<Triple> getDeletedTriples() {
		return deletedTriples;
	}


	@Override
	public PrefixMapping getPrefixMapping() {
		if (pm == null) {
			// copy delegate's prefix mapping.
			pm = new PrefixMappingImpl().setNsPrefixes(base.getPrefixMapping());
		}
		return pm;
	}

	
	@Override
	public boolean isEmpty() {
		if (!addedGraph.isEmpty()) {
			return false;
		}
		if (deletedTriples.isEmpty()) {
			return base.isEmpty();
		}
		return find(Triple.ANY).hasNext();
	}

	
    @Override
    public boolean isIsomorphicWith(Graph g) { 
		return g != null && GraphMatcher.equals(this, g);
    }


	@Override
	public void performAdd(Triple t) {
    	if (deletedTriples.contains(t)) {
    		deletedTriples.remove(t);
    		getEventManager().notifyAddTriple(this, t);
    	}
    	else if (containsByEquals(addedGraph,t) || containsByEquals(base, t) ) {
    		// notify even unsuccessful adds - see javadoc for graphlistener
    		getEventManager().notifyAddTriple(this, t);
        }
    	else {
    		// addedGraph does notification for us.
    		addedGraph.add(t);
    	}
	}


	@Override
	public void performDelete(Triple t) {
		if( containsByEquals(addedGraph,t) ) {
			// addedGraph does notification for us.
			addedGraph.delete(t);
		} 
		else if ( containsByEquals(base, t) ) {
			deletedTriples.add(t);
			getEventManager().notifyDeleteTriple(this, t);
		} 
		else {
			// notify even unsuccessful deletes - see javadoc for graphlistener
			getEventManager().notifyDeleteTriple(this, t);
			return;
		}
	}

	
	@Override
	public int size() {
		return super.size() - deletedTriples.size() + addedGraph.size();
	}
}
