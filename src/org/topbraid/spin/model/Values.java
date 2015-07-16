package org.topbraid.spin.model;

import java.util.List;

import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * A VALUES element (inside of a WHERE clause).
 * 
 * @author Holger Knublauch
 */
public interface Values extends Element {
	
	/**
	 * Gets the bindings (rows), from top to bottom as entered.
	 * @return the Bindings
	 */
	List<Binding> getBindings();

	/**
	 * Gets the names of the declared variables, ordered as entered.
	 * @return the variable names
	 */
	List<String> getVarNames();
}
