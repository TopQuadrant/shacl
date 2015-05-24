package org.topbraid.shacl.constraints;

import java.util.List;

import org.topbraid.shacl.model.SHACLShape;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Encapsulates a single constraint that can be executed, possibly together with pre-bound
 * variables stemming from template calls.
 * 
 * @author Holger Knublauch
 */
public abstract class ConstraintExecutable {
	
	
	/**
	 * Gets the severity level (e.g. sh:Warning).
	 * @return the level class, never null
	 */
	public abstract Resource getSeverity();
	
	
	public abstract List<Literal> getMessages();
	
	
	public abstract Resource getPredicate();
	
	
	public abstract List<SHACLShape> getScopes();
}
