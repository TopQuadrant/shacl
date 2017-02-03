package org.topbraid.shacl.arq;

import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHJSFunction;
import org.topbraid.spin.arq.SPINFunctionDriver;
import org.topbraid.spin.arq.SPINFunctionFactory;


/**
 * A SPINFunctionDriver using sh:scripe to find an executable
 * body for a SHACL function.
 * 
 * This class currently relies on infrastructure from the SPIN API.
 * 
 * @author Holger Knublauch
 */
public class SHACLJSFunctionDriver implements SPINFunctionDriver {

	@Override
	public SPINFunctionFactory create(Resource shaclFunction) {
		return new SHACLJSARQFunction(shaclFunction.as(SHJSFunction.class));
	}
}
