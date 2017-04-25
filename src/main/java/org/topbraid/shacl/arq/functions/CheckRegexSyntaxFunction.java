package org.topbraid.shacl.arq.functions;

import java.util.regex.Pattern;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.topbraid.spin.arq.AbstractFunction1;

public class CheckRegexSyntaxFunction extends AbstractFunction1 {

	@Override
	protected NodeValue exec(Node regexNode, FunctionEnv env) {
		if(regexNode == null || !regexNode.isLiteral()) {
			return NodeValue.makeString("Invalid argument to spif:checkRegexSyntax: " + regexNode);
		}
		else {
			String str = regexNode.getLiteralLexicalForm();
			try {
				Pattern.compile(str);
			}
			catch(Exception ex) {
				return NodeValue.makeString(ex.getMessage());
			}
			throw new ExprEvalException(); // OK
		}
	}
}
