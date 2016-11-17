package org.topbraid.shacl.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.constraints.ConstraintExecutable;
import org.topbraid.shacl.constraints.SPARQLConstraintExecutable;
import org.topbraid.shacl.model.SHSPARQLConstraint;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;

public class SHSPARQLConstraintImpl extends SHResourceImpl implements SHSPARQLConstraint {
	
	public SHSPARQLConstraintImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public List<ConstraintExecutable> getExecutables() {
		List<ConstraintExecutable> results = new ArrayList<ConstraintExecutable>(1);
		results.add(new SPARQLConstraintExecutable(this));
		return results;
	}


	@Override
	public String getSPARQL() {
		return JenaUtil.getStringProperty(this, SH.select);
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

		String sparql = getSPARQL();
		if(sparql != null) {
			return sparql;
		}
		
		return "(Incomplete SPARQL Constraint)";
	}
}
