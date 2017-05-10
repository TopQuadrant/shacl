package org.topbraid.shacl.model.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHRule;
import org.topbraid.shacl.model.SHShape;
import org.topbraid.shacl.vocabulary.SH;
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
	public boolean isDeactivated() {
		return hasProperty(SH.deactivated, JenaDatatypes.TRUE);
	}


	@Override
	public boolean isPropertyShape() {
		return hasProperty(SH.path);
	}
}