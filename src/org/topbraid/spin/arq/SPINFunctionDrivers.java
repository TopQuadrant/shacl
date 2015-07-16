package org.topbraid.spin.arq;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.topbraid.shacl.arq.SHACLSPARQLFunctionDriver;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;


/**
 * The singleton that creates ARQ FunctionFactories from SPIN and SHACL functions.
 * Can be used by applications to install a different singleton with support
 * for different kinds of functions, such as SPINx.
 * 
 * @author Holger Knublauch
 */
public class SPINFunctionDrivers implements SPINFunctionDriver {

	private static SPINFunctionDrivers singleton = new SPINFunctionDrivers();
	
	public static SPINFunctionDrivers get() {
		return singleton;
	}
	
	public static void set(SPINFunctionDrivers value) {
		singleton = value;
	}
	
	
	private Map<Property,SPINFunctionDriver> drivers = new HashMap<Property,SPINFunctionDriver>();
	
	SPINFunctionDrivers() {
		register(SPIN.body, new SPINBodyFunctionDriver());
		register(SH.sparql, new SHACLSPARQLFunctionDriver());
	}


	@Override
	public SPINFunctionFactory create(Resource function) {
		SPINFunctionDriver driver = getDriver(function);
		if(driver != null) {
			return driver.create(function);
		}
		else {
			return null;
		}
	}
	

	/**
	 * Registers a new SPINFunctionDriver for a given key predicate.
	 * For example, SPARQLMotion functions are recognized via sm:body.
	 * Any previous entry will be overwritten.
	 * @param predicate  the key predicate
	 * @param driver  the driver to register
	 */
	public void register(Property predicate, SPINFunctionDriver driver) {
		drivers.put(predicate, driver);
	}
	
	
	private SPINFunctionDriver getDriver(Resource spinFunction) {
		JenaUtil.setGraphReadOptimization(true);
		try {
			SPINFunctionDriver direct = getDirectDriver(spinFunction);
			if(direct != null) {
				return direct;
			}
			else {
				return getDriver(spinFunction, new HashSet<Resource>());
			}
		}
		finally {
			JenaUtil.setGraphReadOptimization(false);
		}
	}
	
	
	private SPINFunctionDriver getDriver(Resource spinFunction, Set<Resource> reached) {
		reached.add(spinFunction);
		for(Resource superClass : JenaUtil.getSuperClasses(spinFunction)) {
			if(!reached.contains(spinFunction)) {
				SPINFunctionDriver superFunction = getDirectDriver(superClass);
				if(superFunction != null) {
					return superFunction;
				}
			}
		}
		return null;
	}
	
	
	private SPINFunctionDriver getDirectDriver(Resource spinFunction) {
		if(!spinFunction.hasProperty(SPIN.abstract_, JenaDatatypes.TRUE) &&
			!spinFunction.hasProperty(SH.abstract_, JenaDatatypes.TRUE)) {
			StmtIterator it = spinFunction.listProperties();
			while(it.hasNext()) {
				Statement s = it.next();
				final SPINFunctionDriver driver = drivers.get(s.getPredicate());
				if(driver != null) {
					it.close();
					return driver;
				}
			}
		}
		return null;
	}
}
