package org.topbraid.spin.arq;

import java.util.HashMap;
import java.util.Map;

import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.model.Function;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.function.FunctionFactory;
import com.hp.hpl.jena.sparql.pfunction.PropertyFunctionFactory;

/**
 * A helper object that can be used to register SPARQL functions
 * (and property functions) per thread, e.g. per servlet request.
 * 
 * @author Holger Knublauch
 */
public class SPINThreadFunctions {
	
	private Map<String,FunctionFactory> functionsCache = new HashMap<String,FunctionFactory>();
	
	private Map<String,PropertyFunctionFactory> pfunctionsCache = new HashMap<String,PropertyFunctionFactory>();

	private Model model;
	
	
	SPINThreadFunctions(Model model) {
		this.model = model;
	}
	
	
	FunctionFactory getFunctionFactory(String uri) {
		FunctionFactory old = functionsCache.get(uri);
		if(old != null) {
			return old;
		}
		else if(functionsCache.containsKey(uri)) {
			return null;
		}
		else {
			return getFunctionFactoryFromModel(uri);
		}
	}
	
	
	PropertyFunctionFactory getPFunctionFactory(String uri) {
		PropertyFunctionFactory old = pfunctionsCache.get(uri);
		if(old != null) {
			return old;
		}
		else if(pfunctionsCache.containsKey(uri)) {
			return null;
		}
		else {
			return getPropertyFunctionFactoryFromModel(uri);
		}
	}


	private FunctionFactory getFunctionFactoryFromModel(String uri) {
		Function spinFunction = model.getResource(uri).as(Function.class);
		if(JenaUtil.hasIndirectType(spinFunction, SPIN.Function) ||
				JenaUtil.hasIndirectType(spinFunction, SH.Function)) {
			FunctionFactory arqFunction = SPINFunctionDrivers.get().create(spinFunction);
			if(arqFunction != null) {
				functionsCache.put(uri, arqFunction);
				return arqFunction;
			}
		}
		// Remember failed attempt for future
		functionsCache.put(uri, null);
		return null;
	}


	private PropertyFunctionFactory getPropertyFunctionFactoryFromModel(String uri) {
		Function spinFunction = model.getResource(uri).as(Function.class);
		if(JenaUtil.hasIndirectType(spinFunction, SPIN.MagicProperty)) {
			if(spinFunction.hasProperty(SPIN.body)) {
				final SPINARQPFunction arqFunction = SPINARQPFunctionFactory.get().create(spinFunction);
				pfunctionsCache.put(uri, arqFunction);
				return arqFunction;
			}
		}
		// Remember failed attempt for future
		pfunctionsCache.put(uri, null);
		return null;
	}
}
