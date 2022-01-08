package org.topbraid.shacl.multifunctions;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.iterator.QueryIteratorWrapper;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.DatasetWithDifferentDefaultModel;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.validation.sparql.SPARQLSubstitutions;
import org.topbraid.shacl.vocabulary.SH;

/**
 * A MultiFunction based on a dash:SPARQLMultiFunction.
 * This essentially wraps a SPARQL SELECT query, iterating over its result bindings on demand,
 * making sure that it gets closed. 
 * 
 * @author Holger Knublauch
 */
public class SPARQLMultiFunction extends AbstractMultiFunction {
	
	public static SPARQLMultiFunction create(Resource declaration) throws IllegalArgumentException {
		
		if(declaration.isAnon()) {
			throw new IllegalArgumentException("Declaration must be a URI node");
		}
		
		String uri = declaration.getURI();
		
		String queryString = JenaUtil.getStringProperty(declaration, SH.select);
		if(queryString == null) {
			throw new IllegalArgumentException("dash:SPARQLMultiFunction " + declaration + " does not declare a sh:select query");
		}
		Query query = ARQFactory.get().createQuery(SPARQLSubstitutions.withPrefixes(queryString, declaration));
		
		return new SPARQLMultiFunction(uri, declaration, query);
	}
	
	private Query query;
	
	
	private SPARQLMultiFunction(String uri, Resource declaration, Query query) {
		super(uri, declaration);
		this.query = query;
	}
	

	@Override
	public QueryIterator execute(List<Node> args, Graph activeGraph, DatasetGraph dataset) {
		QuerySolutionMap initialBindings = new QuerySolutionMap();
		if(args.size() > getParameters().size()) {
			throw new IllegalArgumentException("Too many arguments for multi-function " + getURI() + ": " + args.size() + " >= " + getParameters().size());
		}
		if(activeGraph == null) {
			activeGraph = JenaUtil.createDefaultGraph();
		}
		Model model = ModelFactory.createModelForGraph(activeGraph);
		for(int i = 0; i < args.size(); i++) {
			Node arg = args.get(i);
			if(arg != null) {
				initialBindings.add(getParameters().get(i).getName(), model.asRDFNode(arg));
			}
		}
		for(MultiFunctionParameter param : getParameters()) {
			if(!param.isOptional() && !initialBindings.contains(param.getName())) {
				throw new IllegalArgumentException("Missing value for required multi-function parameter " + param.getName() + " at " + getURI());
			}
		}

		Dataset ds = new DatasetWithDifferentDefaultModel(model, DatasetFactory.wrap(dataset));
		QueryExecution qexec = ARQFactory.get().createQueryExecution(query, ds, initialBindings);
		ResultSet rs = qexec.execSelect();
		Iterator<Binding> bindings = Iter.map(rs, qs -> JenaUtil.asBinding(qs));
		QueryIterator quit = QueryIterPlainWrapper.create(bindings);
		QueryIterator result = QueryIteratorClosing.protect(quit, qexec);

		return result;
	}


	// If anything goes wrong during the iteration from the property function, which is not an ARQ exception,
	// then close the AutoCloseable which is a QueryExecution.
	//
	// QueryIteratorClosing adds try-catch around points where the property function
	// can be involved, and close and cancel cause the closeable to be closed.
	// Assumes the QueryIterator is used properly.
    private static class QueryIteratorClosing extends QueryIteratorWrapper {

        static QueryIterator protect(QueryIterator qIter, AutoCloseable closeable) {
            if (qIter instanceof QueryIteratorClosing) {
                throw new IllegalArgumentException("Wrapping an already wrapped QueryIteratorClosing");
            }
            return new QueryIteratorClosing(qIter, closeable);
        }
        
        private final AutoCloseable closeable;

        private QueryIteratorClosing(QueryIterator qIter, AutoCloseable closeable) {
            super(qIter);
            this.closeable = closeable;
        }
        
        @Override
        protected boolean hasNextBinding() {
            try {
                return super.hasNextBinding() ;
            }
            catch (RuntimeException ex) { closeInternal() ; throw ex; }
        }
        
        @Override
        protected Binding moveToNextBinding() {
            try {
                return super.moveToNextBinding();
            }
            catch (RuntimeException ex) { closeInternal() ; throw ex; }
        }

        @Override
        protected void closeIterator() {
            closeInternal();
            super.closeIterator();
        }
        
        @Override
        protected void requestCancel() {
            closeInternal();
            super.requestCancel();
        }
        
        private void closeInternal() {
            try {
                closeable.close();
            } catch (RuntimeException ex) { 
                throw ex;
            } catch (Exception ex) {
                throw new IllegalStateException("Unexpected checked exception", ex);
            }
        }
    }
}
