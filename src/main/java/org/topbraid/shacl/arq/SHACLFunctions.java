package org.topbraid.shacl.arq;

import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.SPINFunctionDrivers;
import org.topbraid.spin.util.JenaUtil;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.function.FunctionFactory;
import org.apache.jena.sparql.function.FunctionRegistry;

/**
 * Manages globally registered SHACL functions, usually loaded from .shacl.* files.
 * 
 * @author Holger Knublauch
 */
public class SHACLFunctions {
	
	/**
	 * Registers a single SHACL function.
	 * @param resource  the function resource
	 */
	public static void registerFunction(Resource resource) {
		FunctionFactory arqFunction = SPINFunctionDrivers.get().create(resource);
		if(arqFunction != null) {
			FunctionRegistry.get().put(resource.getURI(), arqFunction);
		}
	}

	
	/**
	 * Registers all SHACL functions from a given Model.
	 * @param model  the Model to register the functions from
	 */
	public static void registerFunctions(Model model) {
		Resource shaclFunctionClass = SH.Function.inModel(model);
		for(Resource resource : JenaUtil.getAllInstances(shaclFunctionClass)) {
			if(resource.isURIResource()) {
				registerFunction(resource);
			}
		}
	}
}
