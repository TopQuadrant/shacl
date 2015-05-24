package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLPropertyConstraint;
import org.topbraid.shacl.vocabulary.SHACL;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Default implementation of SHACLPropertyConstraint.
 * 
 * @author Holger Knublauch
 */
public class SHACLPropertyConstraintImpl extends SHACLAbstractPropertyConstraintImpl implements SHACLPropertyConstraint {
	
	public SHACLPropertyConstraintImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}
	
	
	@Override
	public Resource getDefaultType() {
		return JenaUtil.getResourceProperty(this, SHACL.defaultValueType);
	}


	@Override
	public Integer getMaxCount() {
		return JenaUtil.getIntegerProperty(this, SHACL.maxCount);
	}


	@Override
	public Integer getMinCount() {
		return JenaUtil.getIntegerProperty(this, SHACL.maxCount);
	}


	public String toString() {
		return "Property " + getVarName();
	}
}