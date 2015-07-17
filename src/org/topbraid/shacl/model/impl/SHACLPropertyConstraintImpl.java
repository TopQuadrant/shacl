package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLPropertyConstraint;
import org.topbraid.shacl.vocabulary.SH;
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
	public Resource getDefaultValueType() {
		return JenaUtil.getResourceProperty(this, SH.defaultValueType);
	}


	@Override
	public Integer getMaxCount() {
		return JenaUtil.getIntegerProperty(this, SH.maxCount);
	}


	@Override
	public Integer getMinCount() {
		return JenaUtil.getIntegerProperty(this, SH.minCount);
	}


	public String toString() {
		return "Property " + getVarName();
	}
}