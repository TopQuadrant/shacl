package org.topbraid.shacl.validation.java;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;

class UniqueLangConstraintExecutor extends AbstractNativeConstraintExecutor {

	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		if(JenaDatatypes.TRUE.equals(constraint.getParameterValue())) {			
			for(RDFNode focusNode : focusNodes) {
		        Set<String> langs = new HashSet<>();
		        Set<String> reported = new HashSet<>();
				Collection<RDFNode> valueNodes = engine.getValueNodes(constraint, focusNode);
				for(RDFNode valueNode : valueNodes) {
					if(valueNode.isLiteral() && valueNode.asNode().getLiteralLanguage().length() > 0) {
						String lang = valueNode.asNode().getLiteralLanguage();
						if(langs.contains(lang)) {
							if(!reported.contains(lang)) {
								reported.add(lang);
								engine.createValidationResult(constraint, focusNode, null, () -> "Language \"" + lang + "\" used more than once");
							}
						}
						else {
							langs.add(lang);
						}
					}
				}
				engine.checkCanceled();
			}
		}
		addStatistics(constraint, startTime);
	}
}
