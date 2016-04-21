package org.topbraid.shacl.constraints;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLSPARQLConstraint;
import org.topbraid.shacl.model.SHACLShape;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

/**
 * A ConstraintExecutable representing a directly executable constraint,
 * backed by a sh:sparql query.
 * 
 * @author Holger Knublauch
 */
public class SPARQLConstraintExecutable extends ConstraintExecutable {
	
	
	public SPARQLConstraintExecutable(SHACLSPARQLConstraint resource) {
		super(resource);
	}


	@Override
	public List<SHACLShape> getFilterShapes() {
		List<SHACLShape> results = new LinkedList<SHACLShape>();
		for(Resource scope : JenaUtil.getResourceProperties(getConstraint(), SH.filterShape)) {
			results.add(SHACLFactory.asShape(scope));
		}
		return results;
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
