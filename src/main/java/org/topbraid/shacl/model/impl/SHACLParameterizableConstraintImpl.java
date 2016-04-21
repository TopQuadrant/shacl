package org.topbraid.shacl.model.impl;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.constraints.ComponentConstraintExecutable;
import org.topbraid.shacl.constraints.ConstraintExecutable;
import org.topbraid.shacl.model.SHACLConstraintComponent;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLParameterizableConstraint;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class SHACLParameterizableConstraintImpl extends SHACLParameterizableInstanceImpl implements SHACLParameterizableConstraint {
	
	public SHACLParameterizableConstraintImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public List<ConstraintExecutable> getExecutables() {
		List<ConstraintExecutable> results = new LinkedList<ConstraintExecutable>();
		Resource type = JenaUtil.getType(this);
		if(type == null) {
			type = SHACLUtil.getResourceDefaultType(this);
		}
		if(SH.Parameter.equals(type)) {
			type = SH.PropertyConstraint.inModel(type.getModel());
		}
		
		for(Statement s : type.getModel().listStatements(null, SH.context, type).toList()) {
			SHACLConstraintComponent component = SHACLFactory.asConstraintComponent(s.getSubject());
			if(component.getParameters().size() == 1) {
				Property parameter = component.getParameters().get(0).getPredicate();
				for(Statement parameterValueS : this.listProperties(parameter).toList()) {
					results.add(new ComponentConstraintExecutable(this, component, parameterValueS.getObject()));
				}
			}
			else {
				ComponentConstraintExecutable executable = new ComponentConstraintExecutable(this, component);
				if(executable.isComplete()) {
					results.add(executable);
				}
			}
		}
		return results;
	}
}