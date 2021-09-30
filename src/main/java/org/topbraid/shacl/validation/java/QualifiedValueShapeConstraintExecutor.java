package org.topbraid.shacl.validation.java;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Validator for sh:qualifiedValueShape constraints.
 * 
 * Note this is used for both min and max, but min is skipped if max also exists (avoids doing the count twice).
 * 
 * @author Holger Knublauch
 */
class QualifiedValueShapeConstraintExecutor extends AbstractNativeConstraintExecutor {
	
	private boolean disjoint;
	
	private Integer maxCount;
	
	private Integer minCount;
	
	private Set<Resource> siblings = new HashSet<>();
	
	private Resource valueShape;

	
	QualifiedValueShapeConstraintExecutor(Constraint constraint) {
		valueShape = constraint.getShapeResource().getPropertyResourceValue(SH.qualifiedValueShape);
		disjoint = constraint.getShapeResource().hasProperty(SH.qualifiedValueShapesDisjoint, JenaDatatypes.TRUE);
		if(disjoint) {
			for(Resource parent : constraint.getShapeResource().getModel().listSubjectsWithProperty(SH.property, constraint.getShapeResource()).toList()) {
				for(Resource ps : JenaUtil.getResourceProperties(parent, SH.property)) {
					siblings.addAll(JenaUtil.getResourceProperties(ps, SH.qualifiedValueShape));
				}
			}
	        siblings.remove(valueShape);
		}
		maxCount = JenaUtil.getIntegerProperty(constraint.getShapeResource(), SH.qualifiedMaxCount);
		minCount = JenaUtil.getIntegerProperty(constraint.getShapeResource(), SH.qualifiedMinCount);
	}

	
    private boolean hasAnySiblingShape(ValidationEngine engine, Constraint constraint, RDFNode focusNode, RDFNode valueNode) {
        for(Resource sibling : siblings) {
        	Model results = hasShape(engine, constraint, focusNode, valueNode, sibling, true);
        	if(results == null) {
        		return true;
        	}
        }
        return false;
    }

	
	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		
		if(minCount != null && maxCount != null && SH.QualifiedMinCountConstraintComponent.equals(constraint.getComponent())) {
			// Skip minCount constraint if there is also a maxCount constraint at the same shape
			return;
		}
		
		long valueNodeCount = 0;
		for(RDFNode focusNode : focusNodes) {
			int count = 0;
			for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
				valueNodeCount++;
				Model results = hasShape(engine, constraint, focusNode, valueNode, valueShape, true);
				if(results == null && !hasAnySiblingShape(engine, constraint, focusNode, valueNode)) {
					count++;
				}
			}
			if(maxCount != null && count > maxCount) {
				Resource result = engine.createValidationResult(constraint, focusNode, null, () -> "More than " + maxCount + " values have shape " + engine.getLabelFunction().apply(valueShape));
				result.removeAll(SH.sourceConstraintComponent);
				result.addProperty(SH.sourceConstraintComponent, SH.QualifiedMaxCountConstraintComponent);
			}
			if(minCount != null && count < minCount) {
				Resource result = engine.createValidationResult(constraint, focusNode, null, () -> "Less than " + minCount + " values have shape " + engine.getLabelFunction().apply(valueShape));
				result.removeAll(SH.sourceConstraintComponent);
				result.addProperty(SH.sourceConstraintComponent, SH.QualifiedMinCountConstraintComponent);
			}
			engine.checkCanceled();
		}
		addStatistics(engine, constraint, startTime, focusNodes.size(), valueNodeCount);
	}
}
