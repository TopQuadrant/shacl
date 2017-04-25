package org.topbraid.shacl.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHConstraintComponent;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

public class SHConstraintComponentImpl extends SHParameterizableImpl implements SHConstraintComponent {
	
	public SHConstraintComponentImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
	public Resource getValidator(Resource executableType, Resource context) {
		
		Property predicate = SH.PropertyShape.equals(context) ? SH.propertyValidator : SH.nodeValidator;
		Resource validator = JenaUtil.getResourcePropertyWithType(this, predicate, executableType);
		if(validator == null) {
			validator = JenaUtil.getResourcePropertyWithType(this, SH.validator, executableType);
		}
		
		return validator;
	}


	@Override
	public boolean isCore() {
		// Not entirely correct - someone may define their own component in the SH namespace, but close enough
		return SH.NS.equals(getNameSpace()) &&
				!SH.JSConstraintComponent.equals(this) &&
				!SH.SPARQLConstraintComponent.equals(this);
	}
}
