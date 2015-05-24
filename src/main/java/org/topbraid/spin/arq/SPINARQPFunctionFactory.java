package org.topbraid.spin.arq;

import org.topbraid.spin.model.Function;

/**
 * A factory for instances of SPINARQPFunction.
 * This can be overloaded by custom applications to create specific subclasses
 * with different behavior.
 * 
 * @author Holger Knublauch
 */
public class SPINARQPFunctionFactory {

	private static SPINARQPFunctionFactory singleton = new SPINARQPFunctionFactory();
	
	public static SPINARQPFunctionFactory get() {
		return singleton;
	}
	
	public static void set(SPINARQPFunctionFactory value) {
		SPINARQPFunctionFactory.singleton = value;
	}
	
	
	public SPINARQPFunction create(Function functionCls) {
		return new SPINARQPFunction(functionCls);
	}
}
