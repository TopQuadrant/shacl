package org.topbraid.shacl.constraints;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.shacl.model.SHACLConstraintComponent;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLParameter;
import org.topbraid.shacl.model.SHACLParameterizableConstraint;
import org.topbraid.shacl.model.SHACLShape;
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
	
	private SHACLConstraintComponent component;
	
	private SHACLParameterizableConstraint constraint;

	// May be null
	private RDFNode parameterValue;
	
	
	public ComponentConstraintExecutable(SHACLParameterizableConstraint constraint, 
			SHACLConstraintComponent component) {
		super(constraint);
		this.constraint = constraint;
		this.component = component;
	}
	
	
	public ComponentConstraintExecutable(SHACLParameterizableConstraint constraint, 
			SHACLConstraintComponent component, RDFNode parameterValue) {
		this(constraint, component);
		this.parameterValue = parameterValue;
	}
	
	
	public void addBindings(QuerySolutionMap map) {
		
		// Bind ?predicate with sh:predicate (if applicable)
		if(constraint.hasProperty(RDF.type, SH.PropertyConstraint) ||
				constraint.hasProperty(RDF.type, SH.InversePropertyConstraint) ||
				constraint.hasProperty(RDF.type, SH.Parameter)) {
			RDFNode predicate = JenaUtil.getResourceProperty(constraint, SH.predicate);
			String varName = SH.predicateVar.getVarName();
			if(predicate != null && !map.contains(varName)) {
				map.add(varName, predicate);
			}
		}
		
		List<SHACLParameter> params = getComponent().getParameters();
		if(parameterValue != null) {
			SHACLParameter param = params.get(0);
			if(!map.contains(param.getVarName())) {
				map.add(param.getVarName(), parameterValue);
			}
		}
		else {
			Map<String,SHACLParameter> args = getComponent().getParametersMap();
			for(String varName : args.keySet()) {
				if(!map.contains(varName)) {
					SHACLParameter param = args.get(varName);
					RDFNode parameterValue = JenaUtil.getProperty(constraint, param.getPredicate());
					if(parameterValue != null) {
						map.add(varName, parameterValue);
					}
				}
			}
		}
	}


    public SHACLConstraintComponent getComponent() {
		return component;
	}


	@Override
    public List<SHACLShape> getFilterShapes() {
		List<SHACLShape> results = new LinkedList<SHACLShape>();
		for(Resource scope : JenaUtil.getResourceProperties(constraint, SH.filterShape)) {
			results.add(SHACLFactory.asShape(scope));
		}
		return results;
	}
	
	
	@Override
	public List<Literal> getMessages() {
		Resource validator = getValidator();
		if(validator != null) {
			return JenaUtil.getLiteralProperties(validator, SH.message);
		}
		else {
			return Collections.emptyList();
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
		// TODO: Support other languages
		if(SHACLFactory.isNodeConstraint(constraint)) {
			return JenaUtil.getResourceProperty(component, SH.nodeValidator);
		}
		else if(SHACLFactory.isInversePropertyConstraint(constraint)) {
			return JenaUtil.getResourceProperty(component, SH.inversePropertyValidator);
		}
		else {
			return JenaUtil.getResourceProperty(component, SH.propertyValidator);
		}
	}
	
	
	/**
	 * Checks if all non-optional parameters are present.
	 * @return true  if complete
	 */
	public boolean isComplete() {
		for(SHACLParameter param : component.getParameters()) {
			if(!param.isOptional() && !constraint.hasProperty(param.getPredicate())) {
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
