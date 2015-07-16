package org.topbraid.shacl.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.topbraid.shacl.constraints.ConstraintExecutable;
import org.topbraid.shacl.constraints.NativeConstraintExecutable;
import org.topbraid.shacl.model.SHACLNativeConstraint;
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
public class SHACLNativeConstraintImpl extends SHACLResourceImpl implements SHACLNativeConstraint {
	
	public SHACLNativeConstraintImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public List<ConstraintExecutable> getExecutables() {
		List<ConstraintExecutable> results = new ArrayList<ConstraintExecutable>(1);
		results.add(new NativeConstraintExecutable(this));
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
		
		return "(Incomplete SPARQL Constraint)";
	}
}
