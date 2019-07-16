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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.topbraid.jenax.util.JenaUtil;

/**
 * A singleton managing Ontology-based optimizations, to be used (for example) with OptimizedMultiUnions.
 * The contract is that such optimization Objects need to register themselves so that they can
 * get invalidated once an Ontology has changed.
 * 
 * @author Holger Knublauch
 */
public class OntologyOptimizations {

	private static OntologyOptimizations singleton = new OntologyOptimizations();
	
	public static OntologyOptimizations get() {
		return singleton;
	}
	
	public static void set(OntologyOptimizations value) {
		singleton = value;
	}
	
	private boolean enabled;
	
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
	
	// TODO: Switch to Guava caches
	
	@SuppressWarnings("serial")
	public static class MyCache extends LinkedHashMap<Object,Object> {

		public MyCache() {
			super(capacity + 1, 1.1f, true);
		}

		@Override
		protected boolean removeEldestEntry(Entry<Object, Object> eldest) {
			if(size() > capacity) {
				return true;
			}
			else {
				return false;
			}
		}
	};
	
	private Map<Object,Object> objects = Collections.synchronizedMap(new MyCache());
	
	
	public ClassMetadata getClassMetadata(Node cls, Graph graph, String graphKey) {
		Object cacheKey = ClassMetadata.createKey(cls, graphKey);
		ClassMetadata classMetadata = (ClassMetadata)OntologyOptimizations.get().getObject(cacheKey);
		if(classMetadata == null) {
			classMetadata = new ClassMetadata(cls, graphKey);
			OntologyOptimizations.get().putObject(cacheKey, classMetadata);
		}
		return classMetadata;
	}
	
	
	public Object getObject(Object key) {
		return objects.get(key);
	}
	
	
	public Object getOrComputeObject(Object key, Function<Object,Object> function) {
		return objects.computeIfAbsent(key, function);
	}
	
	
	public List<Object> keys() {
		return new ArrayList<>(objects.keySet());
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
		objects.put(key, value);
	}
	
	
	public void reset() {
		objects.clear();
	}
}
