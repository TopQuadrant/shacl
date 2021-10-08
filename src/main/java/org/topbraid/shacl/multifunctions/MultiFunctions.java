package org.topbraid.shacl.multifunctions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.vocabulary.DASH;

/**
 * A singleton managing the (known) MultiFunctions, for example to drive code generators.
 * Any MultiFunction that gets registered will also be installed as a SPARQL property function for Jena.
 * 
 * @author Holger Knublauch
 */
public class MultiFunctions {

	final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

	private static Map<String,MultiFunction> map = new ConcurrentHashMap<>();
	
	
	/**
	 * Gets a MultiFunction with a given URI.
	 * @param uri  the URI of the MultiFunction to get
	 * @return the MultiFunction or null
	 */
	public static MultiFunction get(String uri) {
		return map.get(uri);
	}
	
	
	public static MultiFunction getOrCreate(Resource multiFunction) {
		try {
			return map.computeIfAbsent(multiFunction.getURI(), d -> {
				MultiFunction mf = SPARQLMultiFunction.create(multiFunction);
				PropertyFunctionRegistry.get().put(mf.getURI(), new MultiFunctionPropertyFunction(mf));
				return mf;
			});
		}
		catch(Exception ex) {
			log.error("Failed to install SPARQL property function for multi-function " + multiFunction.getURI(), ex);
			return null;
		}
	}

	
	/**
	 * Registers a MultiFunction and installs a corresponding Jena property function.
	 * @param multiFunction  the MultiFunction
	 */
	public static MultiFunctionPropertyFunction register(MultiFunction multiFunction) {
		map.put(multiFunction.getURI(), multiFunction);
		MultiFunctionPropertyFunction factory = new MultiFunctionPropertyFunction(multiFunction);
		PropertyFunctionRegistry.get().put(multiFunction.getURI(), factory);
		return factory;
	}


	/**
	 * Registers all declarative MultiFunctions (instances of dash:MultiFunction) from a given Model.
	 * This will overwrite any previously known functions and property functions with overlapping URIs.
	 * @param model  the Model to look up the definitions
	 */
	public static void registerAll(Model model) {
		model.listSubjectsWithProperty(RDF.type, DASH.SPARQLMultiFunction).forEachRemaining(multiFunction -> {
			if(multiFunction.isURIResource()) {
				try {
					SPARQLMultiFunction mf = SPARQLMultiFunction.create(multiFunction);
					MultiFunctions.register(mf);
				}
				catch(Exception ex) {
					log.error("Failed to install SPARQL property function for multi-function " + multiFunction.getURI(), ex);
				}
			}
		});
		model.listSubjectsWithProperty(RDF.type, DASH.MultiFunction).forEachRemaining(multiFunction -> {
			MultiFunction mf = MultiFunctions.get(multiFunction.getURI());
			if(mf != null) {
				if(mf instanceof AbstractNativeMultiFunction) {
					// Add the metadata/documentation for a natively installed function
					try {
						((AbstractNativeMultiFunction)mf).initFrom(multiFunction);
					}
					catch(Exception ex) {
						log.error("Failed to install native multi-function " + multiFunction.getURI(), ex);
					}
				}
				// else case should never be reached
			}
			else {
				// If native function is not yet installed, put a placeholder?
				log.error("UNEXPECTED INIT ORDER REACHED");
			}
		});
	}


	/**
	 * Temporarily registers any multi-function that is not registered yet, e.g. for the duration of a test case.
	 * Needs to be followed by unregister calls in a finally block.
	 * @param model  the Model to add the MultiFunctions of
	 * @return the actually added URIs, needed for the unregister call
	 */
	public static Iterable<String> registerAllTemp(Model model) {
		List<String> newURIs = new LinkedList<>();
		Resource mfClass = DASH.SPARQLMultiFunction.inModel(model);
		for(Resource multiFunction : JenaUtil.getAllInstances(mfClass)) {
			if(multiFunction.isURIResource() && MultiFunctions.get(multiFunction.getURI()) == null) {
				try {
					SPARQLMultiFunction mf = SPARQLMultiFunction.create(multiFunction);
					register(mf);
					newURIs.add(multiFunction.getURI());
				}
				catch(Exception ex) {
					log.error("Failed to install SPARQL property function for multi-function " + multiFunction.getURI(), ex);
				}
			}
		}
		return newURIs;
	}
	
	
	/**
	 * Removes a MultiFunction and the corresponding property function.
	 * @param uri  the URI of the multi-function to remove
	 */
	public static void unregister(String uri) {
		PropertyFunctionRegistry.get().remove(uri);
		map.remove(uri);
	}
	

	/**
	 * Gets a stream of the known URIs, which can then be used by the get function.
	 * @return the URIs
	 */
	public static Stream<String> uris() {
		return map.keySet().stream();
	}
}
