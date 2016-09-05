package org.topbraid.spin.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.TransactionHandler ;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;

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
        exec(graph, ()->{
            for(Triple triple : triples) {
                graph.add(triple);
            }
        }) ;
    }

	public static void add(Graph graph, Iterator<Triple> triples) {
		// Avoiding parallel traversal of Iterator (for now) -> copy into List first
		List<Triple> list = new LinkedList<Triple>();
		while(triples.hasNext()) {
			list.add(triples.next());
		}
		add(graph, list);
	}
	
	public static void add(Graph graph, List<Triple> triples) {
	    exec(graph, ()->{
	        for(Triple triple : triples) {
	            graph.add(triple);
	        }
	    }) ;
	}
	
	public static void addInto(Graph graph, Graph src) {
		add(graph, src.find(Triple.ANY));
	}
	
	public static void addInto(Model model, Model src) {
		addInto(model.getGraph(), src.getGraph());
	}
	
	public static void delete(Graph graph, Triple[] triples) {
	    exec(graph, ()->{
	        for(Triple triple : triples) {
	            graph.delete(triple);
	        }
	    }) ;
	}
	
	public static void delete(Graph graph, Iterator<Triple> triples) {
		// Avoiding parallel traversal of Iterator (for now) -> copy into List first
		List<Triple> list = new LinkedList<Triple>();
		while(triples.hasNext()) {
			list.add(triples.next());
		}
		delete(graph, list);
	}
	
	public static void delete(Graph graph, List<Triple> triples) {
	    exec(graph, ()->{
	        for(Triple triple : triples) {
	            graph.delete(triple);
	        }
	    }) ;
	}
	
	public static void deleteFrom(Graph graph, Graph src) {
		delete(graph, src.find(Triple.ANY));
	}

	
    private static void exec(Graph graph, Runnable action) {
        // Temporary revert during 5.2 release.
        if ( true ) {
            // CURRENTLY OFF
            //    Not tested enough for the v5.2 release.
            //    Not tested for MarkLogic or OracleRDF.
            // 
            // Instead, calls to GraphBulkUpdate.addInto etc are each in a transaction.
            // This may be wider than just the GraphBulkUpdate.addInto call.
            
            action.run();
            return ;
        }
        //else {
        //    execTransactional(graph, action);
        //}
    }
    
    
    public static void execTransactional(Graph graph, Runnable action) {

        TransactionHandler handler = graph.getTransactionHandler() ;
        if ( ! handler.transactionsSupported() ) { 
            action.run();
            return ;
        }

        // This works for AutoLockingTDBGraphTxn and SDB.

        // It assumes that either a nested begin() safely throws an exception that does
        // not disturb the transaction or that nested transactions are supported.

        // Any nested transaction support just needs to count nestings so that the final
        // commit is the one that sends all the data and the inner ones do not end the
        // outer transaction.
        try {
            handler.begin();
        } catch(Exception ex) {
            // Run with no new transaction - presumed to be within an existing one.
            action.run();
            return ;
        }

        // Run in a transaction started here. 
        try {
            action.run() ;
            handler.commit() ;
        } catch (Throwable e) { 
            try { handler.abort(); } catch (Throwable e2) { e.addSuppressed(e2); }
            throw e ; 
        }
    }
}
