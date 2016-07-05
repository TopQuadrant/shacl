package org.topbraid.shacl.model.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHPropertyConstraint;
import org.topbraid.shacl.model.SHShape;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class SHShapeImpl extends SHResourceImpl implements SHShape {

	public SHShapeImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public List<SHPropertyConstraint> getPropertyConstraints(RDFNode predicate) {
		List<SHPropertyConstraint> results = new LinkedList<SHPropertyConstraint>();
		for(Resource property : JenaUtil.getResourceProperties(this, SH.parameter)) {
			if(property.hasProperty(SH.predicate, predicate)) {
				results.add(SHFactory.asPropertyConstraint(property));
			}
		}
		for(Resource property : JenaUtil.getResourceProperties(this, SH.property)) {
			if(property.hasProperty(SH.predicate, predicate)) {
				results.add(SHFactory.asPropertyConstraint(property));
			}
		}
		return results;
	}


	@Override
	public boolean isDeactivated() {
		return hasProperty(SH.filterShape, DASH.None);
	}
}
