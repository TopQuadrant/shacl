package org.topbraid.shacl.model.impl;

import org.topbraid.shacl.model.SHACLNativeScope;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Default implementation of SHACLNativeConstraint.
 * 
 * @author Holger Knublauch
 */
public class SHACLNativeScopeImpl extends SHACLResourceImpl implements SHACLNativeScope {
	
	public SHACLNativeScopeImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}


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
