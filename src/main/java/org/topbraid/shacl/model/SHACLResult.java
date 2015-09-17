package org.topbraid.shacl.model;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public interface SHACLResult extends SHACLResource {
	
	String getMessage();
	
	
	Resource getFocusNode();

	
	Resource getSourceConstraint();

	
	Resource getSourceShape();

	
	Resource getSourceTemplate();
	
	
	Resource getSubject();
	
	
	Property getPredicate();
	
	
	RDFNode getObject();
}
