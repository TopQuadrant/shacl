package org.topbraid.shacl.multifunctions;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;

/**
 * Base class for MultiFunctions that return one result variable.
 * Provides a convenient way that merely needs to return an iterator of Nodes.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractMultiFunction1 extends AbstractNativeMultiFunction {
	
	protected AbstractMultiFunction1(String uri, List<String> argVarNames, String resultVarName) {
		super(uri, argVarNames, Collections.singletonList(resultVarName));
	}
	

	/**
	 * Produces the actual result values.
	 * @param args  the arguments from left to right (individual values may be null if optional)
	 * @param activeGraph  the currently active query graph
	 * @param dataset  the dataset
	 * @return the iterator of results
	 */
	protected abstract Iterator<Node> executeIterator(List<Node> args, Graph activeGraph, DatasetGraph dataset);
	

	@Override
	public QueryIterator doExecute(List<Node> args, Graph activeGraph, DatasetGraph dataset) {
		Iterator<Node> it = executeIterator(args, activeGraph, dataset);
		Var resultVar = Var.alloc(getResultVars().get(0).getName());
		Iterator<Binding> bindings = Iter.map(it, node -> BindingBuilder.create().add(resultVar, node).build());
		return QueryIterPlainWrapper.create(bindings);
	}
}
