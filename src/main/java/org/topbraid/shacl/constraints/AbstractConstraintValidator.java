package org.topbraid.shacl.constraints;

import org.apache.jena.rdf.model.Resource;

/**
 * Base class for ResourceConstraintValidator and ModelConstraintValidator.
 * 
 * @author Holger Knublauch
 */
public class AbstractConstraintValidator {
	
	protected Resource report;
	
	
	protected AbstractConstraintValidator(Resource report) {
		this.report = report;
	}
}
