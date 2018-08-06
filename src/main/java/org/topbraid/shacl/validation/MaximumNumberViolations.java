package org.topbraid.shacl.validation;

@SuppressWarnings("serial")
public class MaximumNumberViolations extends RuntimeException {
	
    public MaximumNumberViolations(int violationCount) { 
    	super("Maximum number of violations (" + violationCount + ") reached"); 
    }
}
