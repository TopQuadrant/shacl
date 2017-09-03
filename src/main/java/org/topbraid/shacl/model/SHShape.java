package org.topbraid.shacl.model;

import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public interface SHShape extends SHResource {

	/**
	 * Returns either sh:NodeShape or sh:PropertyShape.
	 * @return the context
	 */
	Resource getContext();
	
	
	/**
	 * Gets the value resource of sh:path or null (for node shapes).
	 * @return the path resource
	 */
	Resource getPath();
	
	
	/**
	 * Gets all property shapes declared for this shape using either sh:parameter or sh:property.
	 * @return the property shapes
	 */
	List<SHPropertyShape> getPropertyShapes();

	
	/**
	 * Gets all property shapes declared for this shape using either sh:parameter or sh:property
	 * that are about a given predicate.
	 * @param predicate  the predicate
	 * @return a possibly empty list
	 */
	List<SHPropertyShape> getPropertyShapes(RDFNode predicate);

	
	/**
	 * Gets the rules attached to this shape via sh:rule.
	 * @return the rules
	 */
	Iterable<SHRule> getRules();
	
	
	/**
	 * Returns the sh:severity of this shape, defaulting to sh:Violation.
	 * @return
	 */
	Resource getSeverity();
	
	
	/**
	 * Checks if a given node is in the target of this shape.
	 * @param node  the node to test
	 * @return true if node is in target
	 */
	boolean hasTargetNode(RDFNode node);
	
	
	/**
	 * Checks if this shape has been deactivated.
	 * @return true if deactivated
	 */
	boolean isDeactivated();
	
	
	/**
	 * Checks if this is a property shape, based on the presence or absence of sh:path.
	 * @return true  iff this has a value for sh:path
	 */
	boolean isPropertyShape();
}
