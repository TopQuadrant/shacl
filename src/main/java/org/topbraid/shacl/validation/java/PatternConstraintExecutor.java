package org.topbraid.shacl.validation.java;

import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.expr.RegexJava;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.validation.AbstractNativeConstraintExecutor;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Validator for sh:pattern constraints.
 * 
 * @author Holger Knublauch
 */
class PatternConstraintExecutor extends AbstractNativeConstraintExecutor {

    private Pattern pattern;
    
    private String patternString;

    private String flagsStr;
	
	
	PatternConstraintExecutor(Constraint constraint) {
		flagsStr = JenaUtil.getStringProperty(constraint.getShapeResource(), SH.flags);
		patternString = JenaUtil.getStringProperty(constraint.getShapeResource(), SH.pattern);
        int flags = RegexJava.makeMask(flagsStr);
        if (flagsStr != null && flagsStr.contains("q")) {
            patternString = Pattern.quote(patternString);
        }
        this.pattern = Pattern.compile(patternString, flags);
	}

	
	@Override
	public void executeConstraint(Constraint constraint, ValidationEngine engine, Collection<RDFNode> focusNodes) {
		long startTime = System.currentTimeMillis();
		long valueNodeCount = 0;
		for(RDFNode focusNode : focusNodes) {
			for(RDFNode valueNode : engine.getValueNodes(constraint, focusNode)) {
				valueNodeCount++;
		        if(valueNode.isAnon()) {
		        	engine.createValidationResult(constraint, focusNode, valueNode, () -> "Blank node cannot match pattern");
		        }
		        else {
			        String str = NodeFunctions.str(valueNode.asNode());
			        if(!pattern.matcher(str).find()) {
			        	engine.createValidationResult(constraint, focusNode, valueNode, () -> "Value does not match pattern \"" + this.patternString + "\"");
			        }
		        }
			}
			engine.checkCanceled();
		}
		addStatistics(engine, constraint, startTime, focusNodes.size(), valueNodeCount);
	}
}
