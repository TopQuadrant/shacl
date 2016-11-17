package org.topbraid.shacl.constraints;

import org.apache.jena.rdf.model.Model;

/**
 * Base class for ResourceConstraintValidator and ModelConstraintValidator.
 * 
 * @author Holger Knublauch
 */
public class AbstractConstraintValidator {
	
	protected Model resultsModel;
	
	
	protected AbstractConstraintValidator(Model resultsModel) {
		this.resultsModel = resultsModel;
	}
}
