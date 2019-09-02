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

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Some utilities operating on Jena Node objects, bypassing the Model/RDFNode abstraction layer.
 * 
 * There should be a cleaner and more complete API for this in the future.
 * 
 * @author Holger Knublauch
 */
public class JenaNodeUtil {
	
	public static Node getObject(Node subject, Node predicate, Graph graph) {
	    ExtendedIterator<Triple> it = graph.find(subject, predicate, Node.ANY);
	    try { 
            return it.hasNext() 
                ? it.next().getObject() 
                : null;
	    } finally { 
	        it.close();
	    }
	}

	
	public static long getObjectCount(Node subject, Node predicate, Graph graph) {
		return Iter.count(graph.find(subject, predicate, Node.ANY));
	}

	
	public static List<Node> getObjects(Node subject, Node predicate, Graph graph) {
		List<Node> results = new LinkedList<>();
		ExtendedIterator<Triple> it = graph.find(subject, predicate, Node.ANY);
		while(it.hasNext()) {
			results.add(it.next().getObject());
		}
		return results;
	}

	
	public static long getSubjectCount(Node predicate, Node object, Graph graph) {
		return Iter.count(graph.find(Node.ANY, predicate, object));
	}
	
	
	public static List<Node> getSubjects(Node predicate, Node object, Graph graph) {
		List<Node> results = new LinkedList<>();
		ExtendedIterator<Triple> it = graph.find(Node.ANY, predicate, object);
		while(it.hasNext()) {
			results.add(it.next().getSubject());
		}
		return results;
	}
	
	
	public static boolean isInstanceOf(Node instance, Node type, Graph graph) {
		// TODO: Use Node API only (maybe worth it)
		Model model = ModelFactory.createModelForGraph(graph);
		return JenaUtil.hasIndirectType((Resource)model.asRDFNode(instance), (Resource)model.asRDFNode(type));
	}
}
