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
package org.topbraid.shacl.optimize;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Cache;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.engine.ShapesGraphFactory;


/**
 * A singleton managing Ontology-based optimizations, to be used (for example) with OptimizedMultiUnions.
 * The contract is that such optimization Objects need to register themselves so that they can
 * get invalidated once an Ontology has changed.
 * 
 * @author Holger Knublauch
 */
public class OntologyOptimizations {

	final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

	private static OntologyOptimizations singleton = new OntologyOptimizations();
	
	public static OntologyOptimizations get() {
		return singleton;
	}
	
	public static void set(OntologyOptimizations value) {
		singleton = value;
	}
	
	private boolean enabled;
	
	private long resetTimeStamp = System.currentTimeMillis();
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean value) {
		enabled = value;
	}
	
	public String getKeyIfEnabledFor(Graph graph) {
		if(enabled && graph instanceof OptimizedMultiUnion) {
			Graph baseGraph = JenaUtil.getBaseGraph(graph);
			if(baseGraph instanceof OntologyOptimizableGraph) {
				if(((OntologyOptimizableGraph)baseGraph).isUsingOntologyOptimizations()) {
					return ((OntologyOptimizableGraph)baseGraph).getOntologyGraphKey();
				}
			}
		}
		return null;
	}
	
	private static final int capacity = 10000;

	private Cache<Object, Object> cache = Caffeine
			.newBuilder()
			.maximumSize(capacity)
			.build();


	public ClassMetadata getClassMetadata(Node cls, Graph graph, String graphKey) {
		Object cacheKey = ClassMetadata.createKey(cls, graphKey);

		ClassMetadata cachedMetadata = (ClassMetadata) getOrComputeObject(cacheKey, (key) -> {
			return new ClassMetadata((Node) key, graphKey);
		});

		return cachedMetadata;
	}



	public Object getObject(Object key) {
		return cache.getIfPresent(key);
	}
	

	// Legacy version with Function parameter
	public Object getOrComputeObject(Object key, Function<Object,Object> function) {
		return cache.get(key, function);
	}

	public ShapesGraph getCachableShapesGraph(String uri) {
		String key = "CachableShapesGraph-" + uri;
		return (ShapesGraph) OntologyOptimizations.get().getOrComputeObject(key, u -> {
			Model shapesModel = ARQFactory.getNamedModel(uri);
			return ShapesGraphFactory.get().createShapesGraph(shapesModel);
		});
	}
	
	public long getResetTimeStamp() {
		return resetTimeStamp;
	}

	
	public List<Object> keys() {
		return new ArrayList<>(cache.asMap().keySet());
	}
	
	
	public void perhapsReset(Graph graph) {
		graph = JenaUtil.getBaseGraph(graph);
		if(graph instanceof OntologyOptimizableGraph) {
			if(((OntologyOptimizableGraph)graph).isOntologyGraph()) {
				reset();
			}
		}
		else if(!JenaUtil.isMemoryGraph(graph)) {
			// Don't reset for example on changes to session graphs or temp graphs
			reset();
		}
	}
	
	
	public void putObject(Object key, Object value) {
		cache.put(key, value);
	}
	
	
	public void reset() {
		cache.invalidateAll();
		resetTimeStamp = System.currentTimeMillis();
	}
}
