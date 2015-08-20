package org.topbraid.shacl.rules;

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
public abstract class RuleExecutable {
	
	private Resource resource;
	
	
	public RuleExecutable(Resource resource) {
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
	
	
	/**
	 * Gets the specified sh:predicate (if any), to be used for constructed results.
	 * @return the predicate or null
	 */
	public Resource getPredicate() {
		return JenaUtil.getResourceProperty(getResource(), SH.predicate);
	}
	
	
	public Resource getResource() {
		return resource;
	}

	
	/**
	 * Gets the severity level (e.g. sh:Warning).
	 * @return the level class, never null
	 */
	public Resource getSalience() {
		Resource result = JenaUtil.getResourceProperty(getResource(), SH.salience);
		return result == null ? SH.Error : result;
	}
	
	
	public abstract SHACLTemplateCall getTemplateCall();
}
