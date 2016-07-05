package org.topbraid.shacl.model;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public interface SHResult extends SHResource {
	
	String getMessage();
	
	
	RDFNode getFocusNode();

	
	Resource getSourceConstraint();
	
	
	Resource getSourceConstraintComponent();

	
	Resource getSourceShape();
	
	
	Resource getPath();
	
	
	RDFNode getValue();
}
