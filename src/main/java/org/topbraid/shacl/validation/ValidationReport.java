package org.topbraid.shacl.validation;

import java.util.List;

public interface ValidationReport {

	boolean conforms();
	
	List<ValidationResult> results();
}
