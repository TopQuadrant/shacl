package org.topbraid.shacl.model.impl;

import java.util.ArrayList;
import java.util.List;


import org.topbraid.shacl.model.SHACLNativeRule;
import org.topbraid.shacl.rules.NativeRuleExecutable;
import org.topbraid.shacl.rules.RuleExecutable;
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
public class SHACLNativeRuleImpl extends SHACLResourceImpl implements SHACLNativeRule {
	
	public SHACLNativeRuleImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public List<RuleExecutable> getExecutables() {
		List<RuleExecutable> results = new ArrayList<RuleExecutable>(1);
		results.add(new NativeRuleExecutable(this));
		return results;
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
		
		String message = JenaUtil.getStringProperty(this, SH.message);
		if(message != null) {
			return message;
		}

		String sparql = JenaUtil.getStringProperty(this, SH.sparql);
		if(sparql != null) {
			return sparql;
		}
		
		return "(Incomplete SPARQL Rule)";
	}
}
