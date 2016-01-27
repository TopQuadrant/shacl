package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLResult;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * Default implementation of SHACLResult.
 * 
 * @author Holger Knublauch
 */
public class SHACLResultImpl extends SHACLResourceImpl implements SHACLResult {
	
	public SHACLResultImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public Resource getFocusNode() {
		return JenaUtil.getResourceProperty(this, SH.focusNode);
	}

	
	@Override
	public String getMessage() {
		return JenaUtil.getStringProperty(this, SH.message);
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
	public Resource getSourceConstraint() {
		return JenaUtil.getResourceProperty(this, SH.sourceConstraint);
	}

	
	@Override
	public Resource getSourceShape() {
		return JenaUtil.getResourceProperty(this, SH.sourceShape);
	}

	
	@Override
	public Resource getSourceTemplate() {
		return JenaUtil.getResourceProperty(this, SH.sourceTemplate);
	}

	
	@Override
	public Resource getSubject() {
		return JenaUtil.getResourceProperty(this, SH.subject);
	}
}