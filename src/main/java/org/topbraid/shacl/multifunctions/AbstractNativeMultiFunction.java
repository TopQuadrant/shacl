package org.topbraid.shacl.multifunctions;

import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.QueryIterator;

/**
 * Base class for all natively (Java) implemented MultiFunctions.
 * They are initialized without the metadata (parameter declarations etc) but that will be added
 * once the corresponding .api. file will be reached.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractNativeMultiFunction extends AbstractMultiFunction {

	protected AbstractNativeMultiFunction(String uri, List<String> argVarNames, List<String> resultVarNames) {
		super(uri, argVarNames, resultVarNames); // Real declaration will be added later from .api. files
	}

	@Override
	public QueryIterator execute(List<Node> args, Graph activeGraph, DatasetGraph dataset) {
		for(int i = 0; i < getParameters().size(); i++) {
			MultiFunctionParameter param = getParameters().get(i);
			if(!param.isOptional() && (args.size() < i || args.get(i) == null)) {
				throw new IllegalArgumentException("Missing value for required multi-function parameter " + param.getName() + " at " + getURI());
			}
		}
		return doExecute(args, activeGraph, dataset);
	}

	
	protected abstract QueryIterator doExecute(List<Node> args, Graph activeGraph, DatasetGraph dataset);
}
