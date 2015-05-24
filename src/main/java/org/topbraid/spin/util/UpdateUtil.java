package org.topbraid.spin.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.modify.request.UpdateDeleteWhere;
import com.hp.hpl.jena.sparql.modify.request.UpdateModify;
import com.hp.hpl.jena.update.Update;


/**
 * Utility on SPARQL Update operations.
 * 
 * @author Holger Knublauch
 */
public class UpdateUtil {
	
	/**
	 * Gets all Graphs that are potentially updated in a given Update request.
	 * @param update  the Update (UpdateModify and UpdateDeleteWhere are supported)
	 * @param dataset  the Dataset to get the Graphs from
	 * @return the Graphs
	 */
	public static Collection<Graph> getUpdatedGraphs(Update update, Dataset dataset, Map<String,RDFNode> templateBindings) {
		Set<Graph> results = new HashSet<Graph>();
		if(update instanceof UpdateModify) {
			addUpdatedGraphs(results, (UpdateModify)update, dataset, templateBindings);
		}
		else if(update instanceof UpdateDeleteWhere) {
			addUpdatedGraphs(results, (UpdateDeleteWhere)update, dataset, templateBindings);
		}
		return results;
	}

	
	private static void addUpdatedGraphs(Set<Graph> results, UpdateDeleteWhere update, Dataset dataset, Map<String,RDFNode> templateBindings) {
		addUpdatedGraphs(results, update.getQuads(), dataset, templateBindings);
	}
	
	
	private static void addUpdatedGraphs(Set<Graph> results, UpdateModify update, Dataset dataset, Map<String,RDFNode> templateBindings) {
		Node withIRI = update.getWithIRI();
		if(withIRI != null) {
			results.add(dataset.getNamedModel(withIRI.getURI()).getGraph());
		}
		addUpdatedGraphs(results, update.getDeleteQuads(), dataset, templateBindings);
		addUpdatedGraphs(results, update.getInsertQuads(), dataset, templateBindings);
	}

	
	private static void addUpdatedGraphs(Set<Graph> results, Iterable<Quad> quads, Dataset graphStore, Map<String,RDFNode> templateBindings) {
		for(Quad quad : quads) {
			if(quad.isDefaultGraph()) {
				results.add(graphStore.getDefaultModel().getGraph());
			}
			else if(quad.getGraph().isVariable()) {
				if(templateBindings != null) {
					String varName = quad.getGraph().getName();
					RDFNode binding = templateBindings.get(varName);
					if(binding != null && binding.isURIResource()) {
						results.add(graphStore.getNamedModel(binding.asNode().getURI()).getGraph());
					}
				}
			}
			else {
				Model namedModel = graphStore.getNamedModel(quad.getGraph().getURI());
				if(namedModel == null) {
					throw new IllegalArgumentException("Cannot resolve named graph " + quad.getGraph().getURI());
				}
				results.add(namedModel.getGraph());
			}
		}
	}
}
