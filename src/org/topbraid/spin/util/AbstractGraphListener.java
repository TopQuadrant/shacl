/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved.
 *******************************************************************************/
package org.topbraid.spin.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.graph.GraphListener;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.IteratorCollection;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

/**
 * An abstract GraphListeners that forwards each call
 * into {@link #notifyAddTriple} and
 * {@link #notifyDeleteTriple} to
 * reduce the implementation burden of subclasses.
 * All of the bulk operations are forwarded to
 * {@link #notifyAddIterator} and
 * {@link #notifyDeleteIterator}.
 * So subclasses can override those two methods to
 * modify all the bulk operations, except the removeAll
 * ones.
 * For the removeAll operations, subclasses should implement
 * {@link #notifyRemoveAll(Graph, Triple)},
 * this is only called by the default implementation
 * of {@link #notifyEvent(Graph, Object)}.
 *
 *
 *
 * @author Holger Knublauch, Jeremy Carroll
 */
public abstract class AbstractGraphListener implements GraphListener {



	public void notifyAddArray(Graph g, Triple[] triples) {
		notifyAddIterator(g,Arrays.asList(triples).iterator());
	}

	public void notifyAddGraph(Graph g, Graph added) {
		notifyAddIterator(g,added.find(Triple.ANY));
	}


	public void notifyAddIterator(Graph g, Iterator<Triple> it) {
		if (it instanceof ClosableIterator) {
			// copy in case the find result is holding locks ...
			notifyAddList(g,IteratorCollection.iteratorToList(it));
		} else {
			while (it.hasNext()) {
				Triple t = it.next();
				notifyAddTriple(g, t);
			}
		}
	}


	public void notifyAddList(Graph g, List<Triple> triples) {
		notifyAddIterator(g, triples.iterator());
	}

	public void notifyDeleteArray(Graph g, Triple[] triples) {
		notifyDeleteIterator(g,Arrays.asList(triples).iterator());
	}
	public void notifyDeleteGraph(Graph g, Graph removed) {
		notifyDeleteIterator(g,removed.find(Triple.ANY));
	}



	public void notifyDeleteIterator(Graph g, Iterator<Triple> it) {
		if (it instanceof ClosableIterator) {
			// copy in case the find result is holding locks ...
			notifyDeleteList(g,IteratorCollection.iteratorToList(it));
		} else {
			while (it.hasNext()) {
				Triple triple = it.next();
				notifyDeleteTriple(g, triple);
			}
		}
	}



	public void notifyDeleteList(Graph g, List<Triple> list) {
		notifyDeleteIterator(g, list.iterator());
	}



	/**
    <code>value</code> is usually a {@link GraphEvents}.
    Special attention is drawn to {@link GraphEvents#removeAll}
    and events whose {@link GraphEvents#getTitle()} is <code>"remove"</code>
    (see {@link GraphEvents#remove(Node, Node, Node)}. These correspond
    to the bulk operations {@link BulkUpdateHandler#removeAll()},
    and {@link BulkUpdateHandler#remove(Node, Node, Node)}, respectively.
    Unlike other notifications, the listener cannot tell which triples
    have been modified, since they have already been deleted by the time
    this event is sent, and the event does not include a record of them.
    This default implementation maps these two events to
    {@link #notifyRemoveAll(Graph, Triple)} calls.
    */
	public void notifyEvent(Graph source, Object value) {
		if (value instanceof GraphEvents) {
			if (GraphEvents.removeAll.equals(value)) {
				notifyRemoveAll(source,Triple.ANY);
			} else {
				GraphEvents event = (GraphEvents)value;
				if ("remove".equals(event.getTitle())) {
					notifyRemoveAll(source,(Triple)event.getContent());
				}
			}
		}
	}

	/**
	 * Called after a removeAll modification. The
	 * actual triples deleted cannot be identified easily.
	 * See {@link #notifyEvent(Graph, Object)} for explanation
	 * of this method.
	 * @param source
	 * @param pattern The pattern of triples being removed, often {@link Triple#ANY}.
	 */
	protected abstract void notifyRemoveAll(Graph source, Triple pattern);
}
