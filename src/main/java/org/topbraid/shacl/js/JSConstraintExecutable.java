package org.topbraid.shacl.js;

import java.util.List;

import org.apache.jena.rdf.model.Literal;
import org.topbraid.shacl.constraints.ConstraintExecutable;
import org.topbraid.shacl.model.SHJSConstraint;

public class JSConstraintExecutable extends ConstraintExecutable {

	public JSConstraintExecutable(SHJSConstraint constraint) {
		super(constraint);
	}

	
	@Override
	public List<Literal> getMessages() {
		// TODO Auto-generated method stub
		return null;
	}
}
