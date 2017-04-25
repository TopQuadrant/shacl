package org.topbraid.shacl.engine;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.model.SHConstraintComponent;
import org.topbraid.shacl.model.SHParameter;
import org.topbraid.shacl.model.SHShape;
import org.topbraid.spin.system.SPINLabels;

/**
 * Represents a shape as input to an engine (e.g. validation or rule).
 * A Shape consists of a collection of Constraints.
 * 
 * @author Holger Knublauch
 */
public class Shape {
	
	private List<Constraint> constraints;

	private SHShape shape;
	
	private ShapesGraph shapesGraph;
	
	
	public Shape(ShapesGraph shapesGraph, SHShape shape) {
		this.shape = shape;
		this.shapesGraph = shapesGraph;
	}
	
	
	public Iterable<Constraint> getConstraints() {
		if(constraints == null) {
			constraints = new LinkedList<>();
			Set<SHConstraintComponent> handled = new HashSet<>();
			for(Statement s : shape.listProperties().toList()) {
				SHConstraintComponent component = shapesGraph.getComponentWithParameter(s.getPredicate());
				if(component != null && !handled.contains(component)) {
					List<SHParameter> params = component.getParameters();
					if(params.size() == 1) {
						Constraint constraint = new Constraint(this, component, params, s.getObject());
						if(!shapesGraph.isIgnoredConstraint(constraint)) {
							constraints.add(constraint);
						}
					}
					else if(isComplete(params)) {
						handled.add(component);
						Constraint constraint = new Constraint(this, component, params, null);
						if(!shapesGraph.isIgnoredConstraint(constraint)) {
							constraints.add(constraint);
						}
					}
				}
			}
		}
		return constraints;
	}
	
	
	public SHShape getShapeResource() {
		return shape;
	}
	
	
	private boolean isComplete(List<SHParameter> params) {
		for(SHParameter param : params) {
			if(!param.isOptional() && !shape.hasProperty(param.getPredicate())) {
				return false;
			}
		}
		return true;
	}
	
	
	public String toString() {
		return SPINLabels.get().getLabel(getShapeResource());
	}
}
