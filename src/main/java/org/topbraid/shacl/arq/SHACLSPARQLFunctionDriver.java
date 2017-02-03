package org.topbraid.shacl.arq;

import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHSPARQLFunction;
import org.topbraid.spin.arq.SPINFunctionDriver;
import org.topbraid.spin.arq.SPINFunctionFactory;


/**
 * A SPINFunctionDriver using sh:sparql to find an executable
 * body for a SHACL function.
 * 
 * This class currently relies on infrastructure from the SPIN API.
 * 
 * @author Holger Knublauch
 */
public class SHACLSPARQLFunctionDriver implements SPINFunctionDriver {

	@Override
	public SPINFunctionFactory create(Resource shaclFunction) {
		return new SHACLSPARQLARQFunction(shaclFunction.as(SHSPARQLFunction.class));
	}
}
