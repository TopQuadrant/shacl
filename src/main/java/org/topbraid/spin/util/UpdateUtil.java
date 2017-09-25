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
package org.topbraid.spin.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.modify.request.UpdateDeleteWhere;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.update.Update;


/**
 * Utility on SPARQL Update operations.
 * 
 * @author Holger Knublauch
 */
public class UpdateUtil {
	
	/**
	 * Gets all Graphs that are potentially updated in a given Update request.
	 * @param update  the Update (UpdateModify and UpdateDeleteWhere are supported)
	 * @param dsg  the Dataset to get the Graphs from
	 * @return the Graphs
	 */
	public static Collection<Graph> getUpdatedGraphs(Update update, DatasetGraph dsg, Map<String,RDFNode> templateBindings) {
		Set<Graph> results = new HashSet<Graph>();
		if(update instanceof UpdateModify) {
			addUpdatedGraphs(results, (UpdateModify)update, dsg, templateBindings);
		}
		else if(update instanceof UpdateDeleteWhere) {
			addUpdatedGraphs(results, (UpdateDeleteWhere)update, dsg, templateBindings);
		}
		return results;
	}

	
	private static void addUpdatedGraphs(Set<Graph> results, UpdateDeleteWhere update, DatasetGraph dsg, Map<String,RDFNode> templateBindings) {
		addUpdatedGraphs(results, update.getQuads(), dsg, templateBindings);
	}
	
	
	private static void addUpdatedGraphs(Set<Graph> results, UpdateModify update, DatasetGraph dsg, Map<String,RDFNode> templateBindings) {
		Node withIRI = update.getWithIRI();
		if(withIRI != null) {
			results.add(dsg.getGraph(withIRI));
		}
		addUpdatedGraphs(results, update.getDeleteQuads(), dsg, templateBindings);
		addUpdatedGraphs(results, update.getInsertQuads(), dsg, templateBindings);
	}

	
	private static void addUpdatedGraphs(Set<Graph> results, Iterable<Quad> quads, DatasetGraph dsg, Map<String,RDFNode> templateBindings) {
		for(Quad quad : quads) {
			if(quad.isDefaultGraph()) {
				results.add(dsg.getDefaultGraph());
			}
			else if(quad.getGraph().isVariable()) {
				if(templateBindings != null) {
					String varName = quad.getGraph().getName();
					RDFNode binding = templateBindings.get(varName);
					if(binding != null && binding.isURIResource()) {
						results.add(dsg.getGraph(binding.asNode()));
					}
				}
			}
			else {
				Graph graph = dsg.getGraph(quad.getGraph());
				if(graph == null) {
					throw new IllegalArgumentException("Cannot resolve named graph " + quad.getGraph().getURI());
				}
				results.add(graph);
			}
		}
	}
}
