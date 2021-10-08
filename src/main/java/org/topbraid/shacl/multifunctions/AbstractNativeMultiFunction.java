package org.topbraid.shacl.multifunctions;

import java.util.List;

/**
 * Base class for all natively (Java) implemented MultiFunctions.
 * They are initialized without the metadata (parameter declarations etc) but that will be added
 * once the corresponding .api. file will be reached.
 * 
 * @author Holger Knublauch
 */
public abstract class AbstractNativeMultiFunction extends AbstractMultiFunction {

	protected AbstractNativeMultiFunction(String uri, List<String> argVarNames, List<String> resultVarNames) {
		super(uri, argVarNames, resultVarNames); // Real declaration will be added later from .api. files
	}
}
