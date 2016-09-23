package org.topbraid.shacl.constraints;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHConstraintComponent;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHParameter;
import org.topbraid.shacl.model.SHParameterizableConstraint;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.util.JenaUtil;

/**
 * A ConstraintExecutable backed by a constraint component.
 * 
 * Since constraint components with a single value may appear more than once,
 * we also store the actual parameter value for each such occurrence.
 * 
 * @author Holger Knublauch
 */
public class ComponentConstraintExecutable extends ConstraintExecutable {
	
	private SHConstraintComponent component;
	
	private SHParameterizableConstraint constraint;
	
	private Resource context;

	// May be null
	private RDFNode parameterValue;
	
	
	public ComponentConstraintExecutable(SHParameterizableConstraint constraint, 
			SHConstraintComponent component, Resource context) {
		super(constraint);
		
		if(!SH.Shape.equals(context) && !SH.PropertyConstraint.equals(context)) {
			throw new IllegalArgumentException("Invalid context: " + context);
		}
		this.constraint = constraint;
		this.component = component;
		this.context = context;
	}
	
	
	public ComponentConstraintExecutable(SHParameterizableConstraint constraint, 
			SHConstraintComponent component, Resource context, RDFNode parameterValue) {
		this(constraint, component, context);
		this.parameterValue = parameterValue;
	}
	
	
	public void addBindings(QuerySolutionMap map) {
		
		List<SHParameter> params = getComponent().getParameters();
		if(parameterValue != null) {
			SHParameter param = params.get(0);
			if(!map.contains(param.getVarName())) {
				map.add(param.getVarName(), parameterValue);
			}
		}
		else {
			Map<String,SHParameter> args = getComponent().getParametersMap();
			for(String varName : args.keySet()) {
				if(!map.contains(varName)) {
					SHParameter param = args.get(varName);
					RDFNode parameterValue = JenaUtil.getProperty(constraint, param.getPredicate());
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
    	return context;
    }
	
	
	@Override
	public List<Literal> getMessages() {
		List<Literal> constraintMessages = JenaUtil.getLiteralProperties(constraint, SH.message);
		if(!constraintMessages.isEmpty()) {
			return constraintMessages;
		}
		else {
			Resource validator = getValidator();
			if(validator != null) {
				return JenaUtil.getLiteralProperties(validator, SH.message);
			}
			else {
				return Collections.emptyList();
			}
		}
	}
	
	
	public RDFNode getParameterValue() {
		return parameterValue;
	}


	@Override
	public Resource getSeverity() {
		Resource override = JenaUtil.getResourceProperty(constraint, SH.severity);
		if(override != null) {
			return override;
		}
		else {
			return super.getSeverity();
		}
	}


	public Resource getValidator() {
		
		Property predicate = SH.PropertyConstraint.equals(context) ? SH.propertyValidator : SH.shapeValidator;
		Resource validator = JenaUtil.getResourceProperty(component, predicate);
		if(validator == null) {
			validator = JenaUtil.getResourceProperty(component, SH.validator);
		}
		
		return validator;
	}
	
	
	/**
	 * Checks if all non-optional parameters are present, also checking for either sh:predicate or sh:path
	 * in the case of property constraints.
	 * @return true  if complete
	 */
	public boolean isComplete() {
		
		for(SHParameter param : component.getParameters()) {
			if(!component.isOptionalParameter(param.getPredicate()) && !constraint.hasProperty(param.getPredicate())) {
				return false;
			}
		}
		
		if(SHFactory.isPropertyConstraint(constraint)) {
			if(!constraint.hasProperty(SH.path) && !constraint.hasProperty(SH.predicate)) {
				return false;
			}
		}
		
		return true;
	}


	@Override
    public String toString() {
		return SPINLabels.get().getLabel(component);
	}
}
