package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLAbstractPropertyConstraint;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Default implementation of SHACLAbstractPropertyConstraint.
 * 
 * @author Holger Knublauch
 */
public abstract class SHACLAbstractPropertyConstraintImpl extends SHACLTemplateCallImpl implements SHACLAbstractPropertyConstraint {
	
	public SHACLAbstractPropertyConstraintImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	
	
	@Override
	public String getComment() {
		return JenaUtil.getStringProperty(this, RDFS.comment);
	}


	@Override
	public Property getPredicate() {
		Resource r = JenaUtil.getResourceProperty(this, SH.predicate);
		if(r != null && r.isURIResource()) {
			return new PropertyImpl(r.asNode(), (EnhGraph)r.getModel());
		}
		else {
			return null;
		}
	}

	
	@Override
	public Resource getValueTypeOrDatatype() {
		Resource valueClass = JenaUtil.getResourceProperty(this, SH.class_);
		if(valueClass != null) {
			return valueClass;
		}
		else {
			Resource directValueType = JenaUtil.getResourceProperty(this, SH.directType);
			if(directValueType != null) {
				return directValueType;
			}
			else {
				return JenaUtil.getResourceProperty(this, SH.datatype);
			}
		}
	}


	@Override
	public String getVarName() {
		Property argProperty = getPredicate();
		if(argProperty != null) {
			return argProperty.getLocalName();
		}
		else {
			return null;
		}
	}
}