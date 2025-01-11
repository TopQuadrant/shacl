package org.topbraid.jenax.util;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.impl.GraphMatcher;
import org.apache.jena.graph.impl.GraphWithPerform;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.HashSet;
import java.util.Set;


/**
 * A WrappedGraph that filters out deleted triples or adds added triples, without
 * modifying the underlying base graph.
 * <p>
 * This class is for single-threaded use only, typically used as temporary graph layer on top of an existing
 * graph for the duration of some algorithm.
 * <p>
 * This runs in two modes, based on the updateBaseGraph flag.
 * <p>
 * By default/legacy (false) the system will only add triples that exist in none of the subgraphs of the delegate graph
 * and claim to delete triples even if they exist in subgraphs only.
 * <p>
 * If true, the adds will always be applied even if one of the subgraphs already contains the triple.
 * This is making sure that transformations will always produce all requested triples.
 * Furthermore this mode is more correct w.r.t. deletes because it will only allow deleting triples from the editable graph.
 *
 * @author Holger Knublauch
 */
public class DiffGraph extends TransparentWrappedGraph {

    /**
     * This graph has additional triples that are not in the delegate.
     */
    protected GraphWithPerform addedGraph = (GraphWithPerform) GraphMemFactory.createDefaultGraph();

    /**
     * This Set has triples that are in the delegate but are excluded from the filtered graph.
     */
    protected Set<Triple> deletedTriples = new HashSet<>();

    private PrefixMapping pm;

    // The graph that the triples will be added to
    private Graph updateableGraph;


    public DiffGraph(Graph delegate) {
        this(delegate, false);
    }


    public DiffGraph(Graph delegate, boolean updateBaseGraph) {
        super(delegate);
        if (updateBaseGraph) {
            updateableGraph = JenaUtil.getBaseGraph(delegate);
        } else {
            updateableGraph = delegate;
        }
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
        return contains(Triple.create(s == null ? Node.ANY : s, p == null ? Node.ANY : p, o == null ? Node.ANY : o));
    }


    @Override
    public boolean contains(Triple t) {
        if (addedGraph.contains(t)) {
            return true;
        } else {
            ExtendedIterator<Triple> it = base.find(t);
            while (it.hasNext()) {
                Triple n = it.next();
                if (!deletedTriples.contains(n)) {
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
    protected boolean containsByEquals(Graph g, Triple t) {
        ExtendedIterator<Triple> it = g.find(t);
        try {
            while (it.hasNext()) {
                if (t.equals(it.next()))
                    return true;
            }
        } finally {
            it.close();
        }
        return false;
    }


    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {

        // First get the underlying base query (without any buffered triples)
        ExtendedIterator<Triple> base = super.find(s, p, o);

        // If deleted triples exist then continue with a filtered iterator
        if (deletedTriples.size() > 0) {
            // base without deleted triples.
            base = base.filterDrop(deletedTriples::contains);
        }

        // If added triples exist then chain the two together
        // this iterator supports remove and removes correctly for this graph
        ExtendedIterator<Triple> added = addedGraph.find(s, p, o);
        if (added.hasNext()) {
            return base.andThen(added);  // base and added are guaranteed disjoint
        } else {
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
        ExtendedIterator<Triple> it = find(Triple.ANY);
        try {
            return !it.hasNext();
        } finally {
            it.close();
        }
    }


    @Override
    public boolean isIsomorphicWith(Graph g) {
        return g != null && GraphMatcher.equals(this, g);
    }


    @Override
    public void performAdd(Triple t) {
        if (deletedTriples.contains(t)) {
            deletedTriples.remove(t);
        } else if (!containsByEquals(addedGraph, t) && !containsByEquals(updateableGraph, t)) {
            addedGraph.add(t);
        }
    }


    @Override
    public void performDelete(Triple t) {
        if (containsByEquals(addedGraph, t)) {
            addedGraph.delete(t);
        } else if (containsByEquals(updateableGraph, t)) {
            deletedTriples.add(t);
        }
    }


    @Override
    public int size() {
        return super.size() - deletedTriples.size() + addedGraph.size();
    }
}
