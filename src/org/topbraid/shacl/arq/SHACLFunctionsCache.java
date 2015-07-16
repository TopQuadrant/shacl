package org.topbraid.shacl.arq;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.NodeValue;

/**
 * A cache that remembers previous calls to SHACL functions marked with sh:cachable.
 * 
 * @author Holger Knublauch
 */
public class SHACLFunctionsCache {

	private static SHACLFunctionsCache singleton = new SHACLFunctionsCache();
	
	public static SHACLFunctionsCache get() {
		return singleton;
	}
	
	public static void set(SHACLFunctionsCache value) {
		SHACLFunctionsCache.singleton = value;
	}
	
	
	private static final int capacity = 10000;
	
	@SuppressWarnings("serial")
	private static class MyCache extends LinkedHashMap<Key,Result> {

		MyCache() {
			super(capacity + 1, 1.1f, true);
		}

		@Override
		protected boolean removeEldestEntry(Entry<Key, Result> eldest) {
			if(size() > capacity) {
				return true;
			}
			else {
				return false;
			}
		}
	};
	
	private Map<Key,Result> cache = Collections.synchronizedMap(new MyCache());

	
	public void clear() {
		cache.clear();
	}
	
	
	public NodeValue execute(SHACLARQFunction function, Dataset dataset, Model defaultModel, QuerySolution bindings, Node[] args) {
		Key key = new Key(function.getSHACLFunction().getURI(), args);
		Result result = cache.get(key);
		if(result == null) {
			result = new Result();
			try {
				result.nodeValue = function.executeBody(dataset, defaultModel, bindings);
			}
			catch(ExprEvalException ex) {
				result.ex = ex;
			}
			cache.put(key, result);
		}
		if(result.ex != null) {
			throw new ExprEvalException(result.ex.getMessage());
		}
		else {
			return result.nodeValue;
		}
	}
	
	
	private static class Key {
		
		private int hashCode;
		
		private Node[] args;
		
		private String functionURI;
		
		
		Key(String functionURI, Node[] args) {
			this.args = args;
			this.functionURI = functionURI;
			hashCode = functionURI.hashCode();
			for(Node arg : args) {
				if(arg != null) {
					hashCode += arg.hashCode();
				}
			}
		}
		
		
		private boolean argEquals(Node arg1, Node arg2) {
			if(arg1 == null) {
				return arg2 == null;
			}
			else if(arg2 == null) {
				return false;
			}
			else {
				return arg1.equals(arg2);
			}
		}
		
		
		@Override
		public boolean equals(Object obj) {
			
			if(!(obj instanceof Key)) {
				return false;
			}
			
			Key other = (Key) obj;
			if(!functionURI.equals(other.functionURI)) {
				return false;
			}
			
			if(args.length != other.args.length) {
				return false;
			}
			
			for(int i = 0; i < args.length; i++) {
				if(!argEquals(args[i], other.args[i])) {
					return false;
				}
			}
			
			return true;
		}
		
		
		@Override
		public int hashCode() {
			return hashCode;
		}
	}
	
	
	private static class Result {
		
		ExprEvalException ex;
		
		NodeValue nodeValue;
	}
}
