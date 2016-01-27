package org.topbraid.spin.arq;

import org.topbraid.spin.model.Function;

import org.apache.jena.rdf.model.Resource;


/**
 * A (default) SPINFunctionDriver using spin:body to find an executable
 * body for a SPIN function.
 * 
 * @author Holger Knublauch
 */
public class SPINBodyFunctionDriver implements SPINFunctionDriver {

	@Override
	public SPINFunctionFactory create(Resource spinFunction) {
		return doCreate(spinFunction.as(Function.class));
	}
	
	
	public static SPINFunctionFactory doCreate(Function spinFunction) {
		return new SPINARQFunction(spinFunction);
	}
}
