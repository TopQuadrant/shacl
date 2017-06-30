package org.topbraid.spin.util;

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
	
	
	public static boolean isInstanceOf(Node instance, Node type, Graph graph) {
		// TODO: Use Node API only (maybe worth it)
		Model model = ModelFactory.createModelForGraph(graph);
		return JenaUtil.hasIndirectType((Resource)model.asRDFNode(instance), (Resource)model.asRDFNode(type));
	}
}
