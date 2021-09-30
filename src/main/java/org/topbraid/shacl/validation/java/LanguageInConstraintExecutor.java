package org.topbraid.shacl.validation.java;

import java.util.Collection;
import java.util.Set;

import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;

/**
 * Validator for sh:languageIn constraints.
 * 
 * @author Holger Knublauch
 */
class LanguageInConstraintExecutor extends AbstractNativeConstraintExecutor {
	
	private Set<String> langs;
	
	LanguageInConstraintExecutor(Constraint constraint) {
		RDFList list = constraint.getParameterValue().as(RDFList.class);
		this.langs = list.iterator().mapWith(n -> n.asLiteral().getString()).toSet();
	}

	
	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		long valueNodeCount = 0;
		for(RDFNode focusNode : focusNodes) {
			for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
				valueNodeCount++;
				if(!valueNode.isLiteral()) {					
					engine.createValidationResult(constraint, focusNode, valueNode, () -> "Not a literal");
				}
				else {
					String lang = valueNode.asLiteral().getLanguage();
					if(lang.isEmpty()) {
						if(!langs.contains(lang)) {							
							engine.createValidationResult(constraint, focusNode, valueNode, () -> "Value without language tag");					
						}
					}
					else if(!langMatches(lang)) {
						engine.createValidationResult(constraint, focusNode, valueNode, () -> "Value does not have a matching language tag");					
					}
				}
			}
			engine.checkCanceled();
		}
		addStatistics(engine, constraint, startTime, focusNodes.size(), valueNodeCount);
	}
	
	
	private boolean langMatches(String l) {
		for(String lang : langs) {
			if(NodeFunctions.langMatches(l, lang)) {
				return true;
			}
		}
		return false;
	}
}
