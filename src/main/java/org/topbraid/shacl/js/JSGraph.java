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
	
	private Set<Stream> openStreams = new HashSet<Stream>();
	
	
	public JSGraph(Graph graph) {
		this.graph = graph;
	}
	
	
	public void close() {
		for(Stream stream : openStreams) {
			stream.closeIterator();
		}
	}
	
	
	public Graph getGraph() {
		return graph;
	}
	
	
	public Stream match(Object subjectSOM, Object predicateSOM, Object objectSOM) {
		Node subject = JSFactory.getNode(subjectSOM);
		Node predicate = JSFactory.getNode(predicateSOM);
		Node object = JSFactory.getNode(objectSOM);
		ExtendedIterator<Triple> it = getGraph().find(subject, predicate, object);
		Stream stream = new Stream(it);
		openStreams.add(stream);
		return stream;
	}
	
	
	public class Stream {
		
		private ExtendedIterator<Triple> it;
		
		
		Stream(ExtendedIterator<Triple> it) {
			this.it = it;
		}
		
		
		public void close() {
			closeIterator();
			openStreams.remove(this);
		}
		
		
		void closeIterator() {
			it.close();
		}
		
		
		public JSTriple read() {
			if(it.hasNext()) {
				Triple triple = it.next();
				JSTriple jsTriple = JSFactory.asJSTriple(triple);
				return jsTriple;
			}
			else {
				close();
				return null;
			}
		}
	}
}
