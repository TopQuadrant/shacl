package org.topbraid.shacl.constraints;

import java.util.List;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHACLConstraint;
import org.topbraid.shacl.model.SHACLShape;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

/**
 * Encapsulates a single constraint that can be executed.
 * 
 * @author Holger Knublauch
 */
public abstract class ConstraintExecutable {
	
	private SHACLConstraint constraint;
	
	
	public ConstraintExecutable(SHACLConstraint constraint) {
		this.constraint = constraint;
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
	public abstract List<Literal> getMessages();
	
	
	public SHACLConstraint getConstraint() {
		return constraint;
	}

	
	/**
	 * Gets the severity level (e.g. sh:Warning).
	 * @return the level class, never null
	 */
	public Resource getSeverity() {
		Resource result = JenaUtil.getResourceProperty(getConstraint(), SH.severity);
		return result == null ? SH.Violation : result;
	}
}
