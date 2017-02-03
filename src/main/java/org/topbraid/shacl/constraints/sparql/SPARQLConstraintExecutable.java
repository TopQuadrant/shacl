package org.topbraid.shacl.constraints.sparql;

import java.util.List;

import org.apache.jena.rdf.model.Literal;
import org.topbraid.shacl.constraints.ConstraintExecutable;
import org.topbraid.shacl.model.SHSPARQLConstraint;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

/**
 * A ConstraintExecutable representing a directly executable constraint,
 * backed by a sh:sparql query.
 * 
 * @author Holger Knublauch
 */
public class SPARQLConstraintExecutable extends ConstraintExecutable {
	
	
	public SPARQLConstraintExecutable(SHSPARQLConstraint resource) {
		super(resource);
	}

	
	@Override
	public List<Literal> getMessages() {
		return JenaUtil.getLiteralProperties(getConstraint(), SH.message);
	}


	@Override
    public String toString() {
		return "SHACL SPARQL Constraint";
	}
}
