package org.topbraid.spin.util;

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
 * @author Holger Knublauch
 */
public class JenaNodeUtil {
	
	public static Node getObject(Node subject, Node predicate, Graph graph) {
		ExtendedIterator<Triple> it = graph.find(subject, predicate, Node.ANY);
		if(it.hasNext()) {
			Node object = it.next().getObject();
			it.close();
			return object;
		}
		else {
			return null;
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
