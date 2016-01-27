package org.topbraid.shacl.model;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public interface SHACLResult extends SHACLResource {
	
	String getMessage();
	
	
	RDFNode getFocusNode();

	
	Resource getSourceConstraint();

	
	Resource getSourceShape();

	
	Resource getSourceTemplate();
	
	
	Resource getSubject();
	
	
	Property getPredicate();
	
	
	RDFNode getObject();
}
