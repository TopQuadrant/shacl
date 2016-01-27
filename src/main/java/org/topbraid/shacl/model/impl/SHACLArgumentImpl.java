package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLArgument;
import org.topbraid.shacl.model.SHACLTemplate;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

/**
 * Default implementation of SHACLArgument.
 * 
 * @author Holger Knublauch
 */
public class SHACLArgumentImpl extends SHACLAbstractPropertyConstraintImpl implements SHACLArgument {
	
	private static final String SH_ARG = SH.NS + "arg";

	
	public SHACLArgumentImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public RDFNode getDefaultValue() {
		Statement s = getProperty(SH.defaultValue);
		if(s != null) {
			return s.getObject();
		}
		else {
			return null;
		}
	}

	
	@Override
	public Integer getOrder() {
		Integer order = JenaUtil.getIntegerProperty(this, SH.order);
		if(order == null) {
			Property predicate = getPredicate();
			if(predicate != null && predicate.getURI().startsWith(SH_ARG)) {
				try {
					return Integer.parseInt(predicate.getURI().substring(SH_ARG.length())) - 1;
				}
				catch(NumberFormatException ex) {
					// Ignore
				}
			}
		}
		return order;
	}
	

	@Override
	public boolean isOptional() {
		return JenaUtil.getBooleanProperty(this, SH.optional);
	}
	
	
	@Override
	public boolean isOptionalAtTemplate(SHACLTemplate template) {
		for(Resource arg : JenaUtil.getResourceProperties(template, SH.argument)) {
			if(arg.hasProperty(SH.predicate, getPredicate())) {
				return false;
			}
		}
		return true;
	}


	public String toString() {
		return "Argument " + getVarName();
	}
}