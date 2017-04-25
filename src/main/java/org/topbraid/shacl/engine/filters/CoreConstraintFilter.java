package org.topbraid.shacl.engine.filters;

import java.util.function.Predicate;

import org.topbraid.shacl.engine.Constraint;

/**
 * Can be used with <code>ShapesGraph.setConstraintFilter</code> to ignore any
 * constraints outside of SHACL Core.
 * 
 * @author Holger Knublauch
 */
public class CoreConstraintFilter implements Predicate<Constraint> {
	
	@Override
	public boolean test(Constraint constraint) {
		return constraint.getComponent().isCore();
	}
}
