package org.topbraid.shacl.arq;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.function.FunctionFactory;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.topbraid.shacl.model.SHACLConstraintComponent;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.SPINFunctionDrivers;
import org.topbraid.spin.util.JenaUtil;

/**
 * Manages globally registered SHACL functions, usually loaded from .shapes.* files.
 * 
 * @author Holger Knublauch
 */
public class SHACLFunctions {
	
	/**
	 * Registers a single SHACL function declared as a sh:Function.
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
		
		Resource ccClass = SH.ConstraintComponent.inModel(model);
		for(Resource resource : JenaUtil.getAllInstances(ccClass)) {
			perhapsRegisterFunction(resource.as(SHACLConstraintComponent.class), SH.nodeValidator);
			perhapsRegisterFunction(resource.as(SHACLConstraintComponent.class), SH.propertyValidator);
			perhapsRegisterFunction(resource.as(SHACLConstraintComponent.class), SH.inversePropertyValidator);
		}
	}
	
	
	private static void perhapsRegisterFunction(SHACLConstraintComponent component, Property predicate) {
		for(Resource validator : JenaUtil.getResourceProperties(component, predicate)) {
			if(validator.isURIResource() && 
					!FunctionRegistry.get().isRegistered(validator.getURI()) &&
					JenaUtil.hasIndirectType(validator, SH.SPARQLAskValidator)) {
				FunctionFactory arqFunction = new SHACLARQFunction(component, validator);
				if(arqFunction != null) {
					FunctionRegistry.get().put(validator.getURI(), arqFunction);
				}
			}
		}
	}
}
