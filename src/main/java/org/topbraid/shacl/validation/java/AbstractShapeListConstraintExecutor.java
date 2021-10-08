package org.topbraid.shacl.validation.java;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;

abstract class AbstractShapeListConstraintExecutor extends AbstractNativeConstraintExecutor {
	
	protected List<Resource> shapes;

	
	AbstractShapeListConstraintExecutor(Constraint constraint) {
		RDFList list = constraint.getParameterValue().as(RDFList.class);
		ExtendedIterator<RDFNode> sit = list.iterator();
		shapes = sit.mapWith(n -> n.asResource()).toList();
	}


	protected String shapeLabelsList(ValidationEngine engine) {
		return shapes.stream().map(shape -> engine.getLabelFunction().apply(shape)).collect(Collectors.joining(", "));
	}
}
