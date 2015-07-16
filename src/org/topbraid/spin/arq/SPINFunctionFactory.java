package org.topbraid.spin.arq;

import com.hp.hpl.jena.sparql.function.FunctionFactory;


/**
 * A marker interface that makes it possible to distinguish SPIN functions
 * from other functions in the FunctionRegistry.
 * 
 * @author Holger Knublauch
 */
public interface SPINFunctionFactory extends FunctionFactory {
}
