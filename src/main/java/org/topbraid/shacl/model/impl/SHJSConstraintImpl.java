package org.topbraid.shacl.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.constraints.ConstraintExecutable;
import org.topbraid.shacl.js.JSConstraintExecutable;
import org.topbraid.shacl.js.SHJS;
import org.topbraid.shacl.model.SHJSConstraint;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;

public class SHJSConstraintImpl extends SHResourceImpl implements SHJSConstraint {
	
	public SHJSConstraintImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public List<ConstraintExecutable> getExecutables() {
		List<ConstraintExecutable> results = new ArrayList<ConstraintExecutable>(1);
		results.add(new JSConstraintExecutable(this));
		return results;
	}
	
	
	@Override
	public String getScript() {
		return JenaUtil.getStringProperty(this, SHJS.script);
	}


	@Override
	public boolean isDeactivated() {
		return hasProperty(SH.deactivated, JenaDatatypes.TRUE);
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
		
		String message = JenaUtil.getStringProperty(this, SH.message);
		if(message != null) {
			return message;
		}

		String script = getScript();
		if(script != null) {
			return script;
		}
		
		return "(Incomplete JavaScript Constraint)";
	}
}
