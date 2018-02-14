package org.topbraid.shacl.arq.functions;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.topbraid.jenax.functions.AbstractFunction1;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Native implementation of dash:isDeactivated.
 * Not that it's a complex function, but it's called very often.
 * 
 * @author Holger Knublauch
 */
public class IsDeactivatedFunction extends AbstractFunction1 {

	@Override
	protected NodeValue exec(Node arg1, FunctionEnv env) {
		return NodeValue.makeBoolean(env.getActiveGraph().contains(arg1, SH.deactivated.asNode(), JenaDatatypes.TRUE.asNode()));
	}
}
