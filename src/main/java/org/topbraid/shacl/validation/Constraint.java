package org.topbraid.shacl.validation;

import java.util.List;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHConstraintComponent;
import org.topbraid.shacl.model.SHParameter;
import org.topbraid.shacl.model.SHShape;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

/**
 * Represents a constraint during validation.
 * Here, a constraint is combination of parameters, e.g. a specific value
 * of sh:datatype at a given shape.
 * 
 * @author Holger Knublauch
 */
public class Constraint {
	
	private SHConstraintComponent component;
	
	private RDFNode parameterValue;
	
	private List<SHParameter> params;

	private Shape shape;
	
	
	public Constraint(Shape shape, SHConstraintComponent component, List<SHParameter> params, RDFNode parameterValue) {
		this.component = component;
		this.params = params;
		this.parameterValue = parameterValue;
		this.shape = shape;
	}

	
	public void addBindings(QuerySolutionMap map) {
		if(parameterValue != null) {
			SHParameter param = params.get(0);
			if(!map.contains(param.getVarName())) {
				map.add(param.getVarName(), parameterValue);
			}
		}
		else {
			for(SHParameter param : params) {
				String varName = param.getVarName();
				if(!map.contains(varName)) {
					RDFNode parameterValue = JenaUtil.getProperty(shape.getShapeResource(), param.getPredicate());
					if(parameterValue != null) {
						map.add(varName, parameterValue);
					}
				}
			}
		}
	}
	
	
	public SHConstraintComponent getComponent() {
		return component;
	}
	
	
	public Resource getContext() {
		if(shape.getShapeResource().hasProperty(SH.path)) {
			return SH.PropertyShape;
		}
		else {
			return SH.NodeShape;
		}
	}
	
	
	public RDFNode getParameterValue() {
		return parameterValue;
	}
	
	
	public SHShape getShapeResource() {
		return shape.getShapeResource();
	}
	
	
	public String toString() {
		return "Constraint " + component.getLocalName() + " at " + shape;
	}
}
