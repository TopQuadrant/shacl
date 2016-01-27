package org.topbraid.spin.arq;

import java.util.Iterator;

import org.apache.jena.query.ARQ;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterBlockTriples;
import org.apache.jena.sparql.engine.main.StageBuilder;
import org.apache.jena.sparql.engine.main.StageGenerator;
import org.apache.jena.sparql.function.FunctionFactory;
import org.apache.jena.sparql.function.FunctionRegistry;

/**
 * An ARQ FunctionRegistry that can be used to associate functions
 * with Threads, so that additional functions from a given Model can
 * be made visible depending on the SPARQL query thread.
 * 
 * <p>Note that this concept only works if ARQ has been set to single
 * threading, which is done by the static block below.</p>
 * 
 * <p>The contract of this class is very strict to prevent memory leaks:
 * Users always need to make sure that unregister is called as soon
 * as a query (block) ends, and to restore any old SPINThreadFunctions
 * object that was registered before.  So a typical block would be:</p>
 * 
 * <code>
 * 	Model model = ... a Model with extra SPIN functions
 * 	SPINThreadFunctions old = SPINThreadFunctionRegistry.register(model);
 * 	try {
 * 		// run SPARQL queries here
 * 	}
 * 	finally {
 * 		SPINThreadFunctionRegistry.unregister(old);
 * 	}</code>
 * 
 * <p>In preparation of the above, the application should start up with code
 * such as</p>
 * 
 * <code>
 * 	FunctionRegistry oldFR = FunctionRegistry.get();
 *  SPINThreadFunctionRegistry threadFR = new SPINThreadFunctionRegistry(oldFR);
 *	FunctionRegistry.set(ARQ.getContext(), threadFR);
 * </code>
 *
 * <p>and do the same for the SPINThreadPropertyFunctionRegistry.</p>
 * 
 * @author Holger Knublauch
 */
public class SPINThreadFunctionRegistry extends FunctionRegistry {

	static {
		// Suppress multi-threading (PatternStage-stuff)
		StageBuilder.setGenerator(ARQ.getContext(), new StageGenerator() {
			public QueryIterator execute(BasicPattern pattern, QueryIterator input,
					ExecutionContext execCxt) {
				return QueryIterBlockTriples.create(input, pattern, execCxt);
			}
		});
	}
	
	private static ThreadLocal<SPINThreadFunctions> localFunctions = new ThreadLocal<SPINThreadFunctions>();
	
	/**
	 * Registers a set of extra SPIN functions from a given Model for the current
	 * Thread.
	 * @param model  the Model containing the SPIN functions
	 * @return any old object that was registered for the current Thread, so that
	 *         the old value can be restored when done.
	 */
	public static SPINThreadFunctions register(Model model) {
		SPINThreadFunctions old = localFunctions.get();
		SPINThreadFunctions neo = new SPINThreadFunctions(model);
		localFunctions.set(neo);
		return old;
	}
	
	
	/**
	 * Unregisters the current Model for the current Thread.
	 * @param old  the old functions that shall be restored or null
	 */
	public static void unregister(SPINThreadFunctions old) {
		if(old != null) {
			localFunctions.set(old);
		}
		else {
			localFunctions.remove();
		}
	}
	
	public static SPINThreadFunctions getFunctions() {
		return localFunctions.get();
	}
	
	private FunctionRegistry base;
	
	public SPINThreadFunctionRegistry(FunctionRegistry base) {
		this.base = base;
	}


	@Override
	public FunctionFactory get(String uri) {
		FunctionFactory b = base.get(uri);
		if(b != null) {
			return b;
		}
		SPINThreadFunctions functions = localFunctions.get();
		if(functions != null) {
			FunctionFactory ff = functions.getFunctionFactory(uri);
			if(ff != null) {
				return ff;
			}
		}
		return null;
	}


	@Override
	public boolean isRegistered(String uri) {
		if(base.isRegistered(uri)) {
			return true;
		}
		else {
			return get(uri) != null;
		}
	}


	@Override
	public Iterator<String> keys() {
		// Note: only returns base keys
		return base.keys();
	}


	@Override
	public void put(String uri, Class<?> funcClass) {
		base.put(uri, funcClass);
	}


	@Override
	public void put(String uri, FunctionFactory f) {
		base.put(uri, f);
	}


	@Override
	public FunctionFactory remove(String uri) {
		return base.remove(uri);
	}
}
