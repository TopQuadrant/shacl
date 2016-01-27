package org.topbraid.spin.util;

import java.util.Iterator;

import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.mem.GraphMemBase;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * This is an extension point for the SPIN library
 * allowing modification of some low level utilities
 * that are exposed through {@link JenaUtil}.
 * 
 * Note: Unstable - should not be used outside of TopBraid.
 * 
 * @author Jeremy Carroll
 */
public class JenaUtilHelper {
	
	/**
	 * Return a multiunion.
	 * @return
	 */
	public MultiUnion createMultiUnion() {
		return new MultiUnion();
	}
	
	
	/**
	 * Return a multiunion, initialized with the given graphs.
	 * @return
	 */
	public MultiUnion createMultiUnion(Iterator<Graph> graphs) {
		return new MultiUnion(graphs);
	}

	
	/**
	 * Return a multiunion, initialized with the given graphs.
	 * @return
	 */
	public MultiUnion createMultiUnion(Graph[] graphs) {
		return new MultiUnion(graphs);
	}
	
	
	/**
	 * A memory graph with no reification.
	 */
	public Graph createDefaultGraph() {
		return Factory.createDefaultGraph();
	}

	
	/**
	 * Returns true if optimizations for faster graphs should
	 * be applied; false if graph is slower. A typical fast graph
	 * is stored in memory, a typical slow graph is stored in a database.
	 * The calling code {@link JenaUtil#isMemoryGraph(Graph)}
	 * deals with {@link MultiUnion}s by taking
	 * the logical AND of the subgraphs.
	 * @param graph A simple graph, not a {@link MultiUnion}
	 * @return true if the graph is fast
	 */
	public boolean isMemoryGraph(Graph graph) {
		return (graph instanceof GraphMemBase);
	}
	
	
	/**
	 * The default implementation does nothing. In TB this is enforced.
	 * @param m
	 * @return
	 */
	public Model asReadOnlyModel(Model m) {
		return m;
	}
	
	
	public Graph asReadOnlyGraph(Graph g) {
		return g;
	}
	
	
	public OntModel createOntologyModel(OntModelSpec spec, Model base) {
		return ModelFactory.createOntologyModel(spec, base);
	}
	
	
	public OntModel createOntologyModel() {
		return ModelFactory.createOntologyModel();
	}
	
	
	public OntModel createOntologyModel(OntModelSpec spec) {
		return ModelFactory.createOntologyModel(spec);
	}
	
	
	public Graph createConcurrentGraph() {
		return createDefaultGraph();
	}
	
	
	public void setGraphReadOptimization(boolean b) {
	}
	
	public Graph deepCloneReadOnlyGraph(Graph g) {
		return asReadOnlyGraph(g);
	}
}