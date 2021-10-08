package org.topbraid.shacl.multifunctions;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHParameter;

/**
 * Metadata about a parameter or result variable of a MultiFunction.
 * 
 * @author Holger Knublauch
 */
public class MultiFunctionParameter {
	
	public static MultiFunctionParameter create(Resource paramR) {
		SHParameter param = SHFactory.asParameter(paramR);
		Resource valueType = param.getClassOrDatatype();
		return new MultiFunctionParameter(param.getVarName(), param.getDescription(), param.isOptional(), valueType != null ? valueType.asNode() : null);
	}
	
	
	private String description;

	private String name;
	
	private boolean optional;
	
	private Node valueType;
	
	
	public MultiFunctionParameter(String name, String description, boolean optional, Node valueType) {
		this.description = description;
		this.name = name;
		this.optional = optional;
		this.valueType = valueType;
	}
	
	
	public final String getName() {
		return name;
	}
	
	
	public final String getDescription() {
		return description;
	}
	
	
	public final Node getValueType() {
		return valueType;
	}
	
	
	public final Var getVar() {
		return Var.alloc(name);
	}
	
	
	public final boolean isOptional() {
		return optional;
	}
}
