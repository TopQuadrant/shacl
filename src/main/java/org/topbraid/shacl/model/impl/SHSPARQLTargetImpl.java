package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.model.SHSPARQLTarget;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class SHSPARQLTargetImpl extends SHResourceImpl implements SHSPARQLTarget {
	
	public SHSPARQLTargetImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}


	@Override
	public String getSPARQL() {
		return JenaUtil.getStringProperty(this, SH.select);
	}


	@Override
    public String toString() {

		String label = JenaUtil.getStringProperty(this, RDFS.label);
		if(label != null) {
			return label;
		}
		
		String comment = JenaUtil.getStringProperty(this, RDFS.comment);
		if(comment != null) {
			return comment;
		}

		String sparql = getSPARQL();
		if(sparql != null) {
			return sparql;
		}
		
		return "(Incomplete SPARQL Target)";
	}
}
