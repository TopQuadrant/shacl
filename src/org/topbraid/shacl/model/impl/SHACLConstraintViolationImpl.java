package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLConstraintViolation;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Default implementation of SHACLConstraintViolation.
 * 
 * @author Holger Knublauch
 */
public class SHACLConstraintViolationImpl extends SHACLResourceImpl implements SHACLConstraintViolation {
	
	public SHACLConstraintViolationImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public String getMessage() {
		return JenaUtil.getStringProperty(this, SH.message);
	}

	
	@Override
	public Resource getRoot() {
		return JenaUtil.getResourceProperty(this, SH.root);
	}

	
	@Override
	public RDFNode getObject() {
		return JenaUtil.getProperty(this, SH.object);
	}

	
	@Override
	public Property getPredicate() {
		Resource value = JenaUtil.getPropertyResourceValue(this, SH.predicate);
		if(value != null) {
			return JenaUtil.asProperty(value);
		}
		else {
			return null;
		}
	}

	
	@Override
	public Resource getSource() {
		return JenaUtil.getResourceProperty(this, SH.source);
	}

	
	@Override
	public Resource getSubject() {
		return JenaUtil.getResourceProperty(this, SH.subject);
	}
}