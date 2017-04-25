package org.topbraid.shacl.validation;

import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.shacl.engine.Constraint;

/**
 * Interface for objects that can execute a given constraint.
 * 
 * Implementation of this include those using SPARQL or JavaScript constraint components
 * but also special handlers for sh:property, sh:sparql and sh:js.
 * 
 * @author Holger Knublauch
 */
public interface ConstraintExecutor {

	void executeConstraint(Constraint constraint, ValidationEngine engine, List<RDFNode> focusNodes);
}
