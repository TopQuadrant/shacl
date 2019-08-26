package org.topbraid.shacl.validation.java;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.vocabulary.SH;

class ClosedConstraintExecutor extends AbstractNativeConstraintExecutor {
	
	private Set<RDFNode> allowedProperties = new HashSet<>();
	
	private boolean closed;

	
	ClosedConstraintExecutor(Constraint constraint) {
		this.closed = constraint.getShapeResource().hasProperty(SH.closed, JenaDatatypes.TRUE);
		RDFList list = JenaUtil.getListProperty(constraint.getShapeResource(), SH.ignoredProperties);
		if(list != null) {
			list.iterator().forEachRemaining(allowedProperties::add);
		}
		for(Resource ps : JenaUtil.getResourceProperties(constraint.getShapeResource(), SH.property)) {
			Resource path = JenaUtil.getResourceProperty(ps, SH.path);
			if(path.isURIResource()) {
				allowedProperties.add(path);
			}
		}
	}

	
	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		if(closed) {
			for(RDFNode focusNode : focusNodes) {
				if(focusNode instanceof Resource) {
					Iterator<Statement> it = ((Resource)focusNode).listProperties();
					while(it.hasNext()) {
						Statement s = it.next();
						if(!allowedProperties.contains(s.getPredicate())) {
							Resource result = engine.createValidationResult(constraint, focusNode, s.getObject(), () -> "Predicate " + engine.getLabelFunction().apply(s.getPredicate()) + " is not allowed (closed shape)");
							result.removeAll(SH.resultPath);
							result.addProperty(SH.resultPath, s.getPredicate());
						}
					}
				}
				engine.checkCanceled();
			}
		}
		addStatistics(constraint, startTime);
	}
}
