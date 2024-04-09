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

/**
 * Validator for sh:closed constraints.
 * 
 * @author Holger Knublauch
 */
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
			Resource path = ps.getPropertyResourceValue(SH.path);
			if(path.isURIResource()) {
				allowedProperties.add(path);
			}
		}
	}

	
	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		if(closed) {
			long startTime = System.currentTimeMillis();
			for(RDFNode focusNode : focusNodes) {
				for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
					if(valueNode instanceof Resource) {
						Iterator<Statement> it = ((Resource)valueNode).listProperties();
						while(it.hasNext()) {
							Statement s = it.next();
							if(!allowedProperties.contains(s.getPredicate())) {
								Resource result = engine.createValidationResult(constraint, valueNode, s.getObject(), () -> "Predicate " + engine.getLabelFunction().apply(s.getPredicate()) + " is not allowed (closed shape)");
								result.removeAll(SH.resultPath);
								result.addProperty(SH.resultPath, s.getPredicate());
							}
						}
						engine.checkCanceled();
					}
				}
			}
			addStatistics(engine, constraint, startTime, focusNodes.size(), focusNodes.size());
		}
	}
}
