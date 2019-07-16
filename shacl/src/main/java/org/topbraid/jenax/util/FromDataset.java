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
package org.topbraid.jenax.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;


/**
 * A Dataset that wraps another Dataset but changes its default and
 * named graphs based on the FROM and FROM NAMED clauses of a given
 * Query.
 * 
 * @author Holger Knublauch
 */
public class FromDataset extends DelegatingDataset {
	
	private Set<String> defaultGraphs;
	
	private Model defaultModel;

	private Set<String> namedGraphs;
	
	
	public FromDataset(Dataset delegate, Query query) throws GraphNotFoundException {
		super(delegate);
		defaultGraphs = new HashSet<String>(query.getGraphURIs());
		namedGraphs = new HashSet<String>(query.getNamedGraphURIs());
		initDefaultModel();
	}


	@Override
	public boolean containsNamedModel(String uri) {
		if(namedGraphs.isEmpty()) {
			return true;
		}
		else {
			return namedGraphs.contains(uri);
		}
	}


	@Override
	public Model getDefaultModel() {
		return defaultModel;
	}


	private void initDefaultModel() throws GraphNotFoundException {
		if(defaultGraphs.isEmpty()) {
			defaultModel = super.getDefaultModel();
		}
		else {
			if(defaultGraphs.size() == 1) {
				String defaultGraphURI = defaultGraphs.iterator().next();
				defaultModel = getNamedModel(defaultGraphURI);
				if(defaultModel == null) {
					throw new GraphNotFoundException("Named graph " + defaultGraphURI + " not found");
				}
			}
			else {
				MultiUnion multiUnion = JenaUtil.createMultiUnion();
				for(String graphURI : defaultGraphs) {
					Model model = getNamedModel(graphURI);
					if(model == null) {
						throw new GraphNotFoundException("Named graph " + graphURI + " not found");
					}
					multiUnion.addGraph(model.getGraph());
				}
				defaultModel = ModelFactory.createModelForGraph(multiUnion);
			}
		}
	}


	@Override
	public Iterator<String> listNames() {
		if(namedGraphs.isEmpty()) {
			return super.listNames();
		}
		else {
			return namedGraphs.iterator();
		}
	}


    @Override
    public boolean isEmpty() {
        return 
            defaultModel.isEmpty() &&
            namedGraphs.stream().map(name->getNamedModel(name)).allMatch(model->model.isEmpty());
    }
}
