package org.topbraid.shacl.arq;

import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHJSFunction;
import org.topbraid.shacl.model.SHSPARQLFunction;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.SPINFunctionDriver;
import org.topbraid.spin.arq.SPINFunctionFactory;


/**
 * A SPINFunctionDriver for SHACL functions.
 * 
 * This class currently relies on infrastructure from the SPIN API.
 * 
 * @author Holger Knublauch
 */
public class SHACLFunctionDriver implements SPINFunctionDriver {
	
	private static boolean jsPreferred = false;
	
	public static void setJSPreferred(boolean value) {
		jsPreferred = value;
	}

	
	@Override
	public SPINFunctionFactory create(Resource shaclFunction) {
		if(jsPreferred) {
			if(shaclFunction.hasProperty(SH.jsLibrary)) {
				return new SHACLJSARQFunction(shaclFunction.as(SHJSFunction.class));
			}
			else {
				return new SHACLSPARQLARQFunction(shaclFunction.as(SHSPARQLFunction.class));
			}
		}
		else if(shaclFunction.hasProperty(SH.ask) || shaclFunction.hasProperty(SH.select)) {
			return new SHACLSPARQLARQFunction(shaclFunction.as(SHSPARQLFunction.class));
		}
		else {
			return new SHACLJSARQFunction(shaclFunction.as(SHJSFunction.class));
		}
	}
}
