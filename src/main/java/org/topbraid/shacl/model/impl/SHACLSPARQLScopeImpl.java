package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.model.SHACLSPARQLScope;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class SHACLSPARQLScopeImpl extends SHACLResourceImpl implements SHACLSPARQLScope {
	
	public SHACLSPARQLScopeImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}


	@Override
	public String getSPARQL() {
		return JenaUtil.getStringProperty(this, SH.sparql);
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

		String sparql = JenaUtil.getStringProperty(this, SH.sparql);
		if(sparql != null) {
			return sparql;
		}
		
		return "(Incomplete SPARQL Scope)";
	}
}
