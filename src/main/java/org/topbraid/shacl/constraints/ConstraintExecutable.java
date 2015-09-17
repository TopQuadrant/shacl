package org.topbraid.shacl.constraints;

import java.util.List;

import org.topbraid.shacl.model.SHACLShape;
import org.topbraid.shacl.model.SHACLTemplateCall;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Encapsulates a single constraint that can be executed, possibly together with pre-bound
 * variables stemming from template calls.
 * 
 * @author Holger Knublauch
 */
public abstract class ConstraintExecutable {
	
	private Resource resource;
	
	
	public ConstraintExecutable(Resource resource) {
		this.resource = resource;
	}
	
	
	/**
	 * Gets the specified sh:filterShapes, to be used as pre-conditions.
	 * @return the filter shapes
	 */
	public abstract List<SHACLShape> getFilterShapes();
	
	
	/**
	 * Gets the specified sh:messages, to be used for constructed results.
	 * @return the messages (may be empty)
	 */
	public List<Literal> getMessages() {
		return JenaUtil.getLiteralProperties(getResource(), SH.message);
	}
	
	
	public Resource getResource() {
		return resource;
	}

	
	/**
	 * Gets the severity level (e.g. sh:Warning).
	 * @return the level class, never null
	 */
	public Resource getSeverity() {
		Resource result = JenaUtil.getResourceProperty(getResource(), SH.severity);
		return result == null ? SH.Violation : result;
	}
	
	
	public abstract SHACLTemplateCall getTemplateCall();
}
