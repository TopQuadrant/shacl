package org.topbraid.shacl.model.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHPropertyShape;
import org.topbraid.shacl.model.SHNodeShape;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class SHNodeShapeImpl extends SHShapeImpl implements SHNodeShape {

	public SHNodeShapeImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public Resource getContext() {
		return SH.NodeShape.inModel(getModel());
	}


	@Override
	public List<SHPropertyShape> getPropertyConstraints(RDFNode predicate) {
		List<SHPropertyShape> results = new LinkedList<SHPropertyShape>();
		for(Resource property : JenaUtil.getResourceProperties(this, SH.parameter)) {
			if(property.hasProperty(SH.path, predicate)) {
				results.add(SHFactory.asPropertyConstraint(property));
			}
		}
		for(Resource property : JenaUtil.getResourceProperties(this, SH.property)) {
			if(property.hasProperty(SH.path, predicate)) {
				results.add(SHFactory.asPropertyConstraint(property));
			}
		}
		return results;
	}
}
