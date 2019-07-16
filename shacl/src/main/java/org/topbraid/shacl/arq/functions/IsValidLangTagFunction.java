package org.topbraid.shacl.arq.functions;

import org.apache.jena.graph.Node;
import org.apache.jena.riot.web.LangTag;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.topbraid.jenax.functions.AbstractFunction1;

/**
 * The SPARQL function spif:isValidLangTag.
 * 
 * @author Holger Knublauch
 */
public class IsValidLangTagFunction extends AbstractFunction1 {

	@Override
	protected NodeValue exec(Node arg, FunctionEnv env) {
		if(arg == null || !arg.isLiteral()) {
			throw new ExprEvalException("Argument must be a (string) literal");
		}
		return NodeValue.makeBoolean(LangTag.check(arg.getLiteralLexicalForm()));
	}
}
