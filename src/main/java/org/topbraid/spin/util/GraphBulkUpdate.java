package org.topbraid.spin.util;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Utility methods that allow switching between the deprecated BulkUpdateHandler
 * and the supposed replacement in GraphUtil later.
 * 
 * Former usages of BulkUpdateHandler in TopBraid have been replaced with those methods.
 * 
 * In TopBraid, we cannot use the default implementation of GraphUtil.add because
 * they first call performAdd and then graph.getEventManager().notifyAdd.
 * In comparison, AbstractDelegatingGraph.add calls performAdd followed by
 * graph.getDelegate().getEventManager(), which is the expected behavior.
 * For example with CachingGraph, its own EventManager does not have listeners attached
 * to it, while its delegate (e.g. SDB) has a listener that updates the cache when a
 * triple was added.
 * 
 * @author Holger Knublauch
 */
public class GraphBulkUpdate {
	
	public static void add(Graph graph, Triple[] triples) {
		graph.getBulkUpdateHandler().add(triples);
	}

	
	public static void add(Graph graph, Iterator<Triple> triples) {
		graph.getBulkUpdateHandler().add(triples);
	}
	
	
	public static void add(Graph graph, List<Triple> triples) {
		graph.getBulkUpdateHandler().add(triples);
	}
	
	
	public static void addInto(Graph graph, Graph src) {
		graph.getBulkUpdateHandler().add(src);
	}
	
	
	public static void addInto(Model model, Model src) {
		addInto(model.getGraph(), src.getGraph());
	}
	
	
	public static void delete(Graph graph, Triple[] triples) {
		graph.getBulkUpdateHandler().delete(triples);
	}
	
	
	public static void delete(Graph graph, Iterator<Triple> triples) {
		graph.getBulkUpdateHandler().delete(triples);
	}
	
	
	public static void delete(Graph graph, List<Triple> triples) {
		graph.getBulkUpdateHandler().delete(triples);
	}
	
	
	public static void deleteFrom(Graph graph, Graph src) {
		graph.getBulkUpdateHandler().delete(src);
	}
}
