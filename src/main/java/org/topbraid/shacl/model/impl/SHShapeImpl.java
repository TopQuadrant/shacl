package org.topbraid.shacl.model.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.model.SHRule;
import org.topbraid.shacl.model.SHShape;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;

public abstract class SHShapeImpl extends SHParameterizableInstanceImpl implements SHShape {
	
	public SHShapeImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}


	@Override
	public Resource getPath() {
		return JenaUtil.getResourceProperty(this, SH.path);
	}


	@Override
	public Iterable<SHRule> getRules() {
		List<SHRule> results = new LinkedList<SHRule>();
		for(Resource r : JenaUtil.getResourceProperties(this, SH.rule)) {
			results.add(r.as(SHRule.class));
		}
		return results;
	}


	@Override
	public Resource getSeverity() {
		Resource result = JenaUtil.getResourceProperty(this, SH.severity);
		return result != null ? result : SH.Violation;
	}


	@Override
	public boolean hasTargetNode(RDFNode node) {
		
		// rdf:type / sh:targetClass
		if(node instanceof Resource) {
			for(Resource type : JenaUtil.getAllTypes((Resource)node)) {
				if(JenaUtil.hasIndirectType(type, SH.Shape)) {
					return true;
				}
				if(hasProperty(SH.targetClass, type)) {
					return true;
				}
			}
		}
		
		// property targets
		if(node instanceof Resource) {
			for(Statement s : listProperties(SH.targetSubjectsOf).toList()) {
				if(((Resource)node).hasProperty(JenaUtil.asProperty(s.getResource()))) {
					return true;
				}
			}
		}
		for(Statement s : listProperties(SH.targetObjectsOf).toList()) {
			if(node.getModel().contains(null, JenaUtil.asProperty(s.getResource()), node)) {
				return true;
			}
		}
		
		if(hasProperty(SH.targetNode, node)) {
			return true;
		}
		
		// sh:target
		for(Statement s : listProperties(SH.target).toList()) {
			if(SHACLUtil.isInTarget(node, ARQFactory.get().getDataset(node.getModel()), s.getResource())) {
				return true;
			}
		}

		return false;
	}


	@Override
	public boolean isDeactivated() {
		return hasProperty(SH.deactivated, JenaDatatypes.TRUE);
	}


	@Override
	public boolean isPropertyShape() {
		return hasProperty(SH.path);
	}
}