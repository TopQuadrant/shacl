package org.topbraid.shacl.js;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.topbraid.shacl.js.model.JSFactory;
import org.topbraid.shacl.js.model.JSTriple;

public class JSGraph {
	
	private Graph graph;
	
	private Set<JSTripleIterator> openIterators = new HashSet<>();
	
	
	public JSGraph(Graph graph) {
		this.graph = graph;
	}
	
	
	public void close() {
		for(JSTripleIterator stream : openIterators) {
			stream.closeIterator();
		}
	}
	
	
	public Graph getGraph() {
		return graph;
	}
	
	
	public JSTripleIterator find(Object subjectSOM, Object predicateSOM, Object objectSOM) {
		Node subject = JSFactory.getNode(subjectSOM);
		Node predicate = JSFactory.getNode(predicateSOM);
		Node object = JSFactory.getNode(objectSOM);
		ExtendedIterator<Triple> it = getGraph().find(subject, predicate, object);
		JSTripleIterator jsit = new JSTripleIterator(it);
		openIterators.add(jsit);
		return jsit;
	}
	
	
	public class JSTripleIterator {
		
		private ExtendedIterator<Triple> it;
		
		
		JSTripleIterator(ExtendedIterator<Triple> it) {
			this.it = it;
		}
		
		
		public void close() {
			closeIterator();
			openIterators.remove(this);
		}
		
		
		void closeIterator() {
			it.close();
		}
		
		
		public JSTriple next() {
			if(it.hasNext()) {
				Triple triple = it.next();
				return JSFactory.asJSTriple(triple);
			}
			else {
				close();
				return null;
			}
		}
	}
}
