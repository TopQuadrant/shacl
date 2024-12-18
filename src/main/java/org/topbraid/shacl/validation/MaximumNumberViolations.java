package org.topbraid.shacl.validation;

public class MaximumNumberViolations extends RuntimeException {

    public MaximumNumberViolations(int violationCount) {
        super("Maximum number of violations (" + violationCount + ") reached");
    }
}
