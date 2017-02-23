package org.topbraid.shacl.js;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.topbraid.shacl.js.model.JSFactory;
import org.topbraid.shacl.js.model.JSTriple;
import org.topbraid.shacl.util.FailureLog;
import org.topbraid.spin.util.ExceptionUtil;

public class JSGraph {
	
	protected JSScriptEngine engine;
	
	private Graph graph;
	
	private Set<JSTripleIterator> openIterators = new HashSet<>();
	
	
	public JSGraph(Graph graph, JSScriptEngine engine) {
		this.engine = engine;
		this.graph = graph;
	}
	
	
	public void close() {
		if(!openIterators.isEmpty()) {
			FailureLog.get().logFailure("JavaScript graph session ended but " + openIterators.size() + " iterators have not been closed. Make sure to close them programmatically to avoid resource leaks and locking problems.");
		}
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
	
	
	public Object query() {
		try {
			return engine.invokeFunctionOrdered("RDFQuery", new Object[] { this });
		}
		catch(Exception ex) {
			throw ExceptionUtil.throwUnchecked(ex);
		}
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
