/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
package org.topbraid.shacl.arq.functions;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.topbraid.jenax.functions.AbstractFunction1;

public class CheckRegexSyntaxFunction extends AbstractFunction1 {

	@Override
	protected NodeValue exec(Node regexNode, FunctionEnv env) {
		if(regexNode == null || !regexNode.isLiteral()) {
			return NodeValue.makeString("Invalid argument to spif:checkRegexSyntax: " + regexNode);
		}

		String str = regexNode.getLiteralLexicalForm();
		try {
			Pattern.compile(str);
		}
		catch(Exception ex) {
			String message = (ex instanceof PatternSyntaxException) ?
					this.buildSystemIndependentMessage((PatternSyntaxException) ex) :
					ex.getMessage();
			return NodeValue.makeString(message);
		}

		throw new ExprEvalException(); // OK
	}

	/**
	 * Convert the specified exception's message to a system-independent
	 * format while preserving the message's embedded regex unchanged.
	 * This allows whoever catches the exception to inspect the original regex
	 * unchanged.
	 * 
	 * @see PatternSyntaxException#getMessage()
	 */
	private String buildSystemIndependentMessage(PatternSyntaxException ex) {
		String message = ex.getMessage();
		if ( ! System.lineSeparator().contains("\r")) {
			return message;
		}

		// the message will *not* be null and
		// it will contain at least a single line separator before the
		// embedded pattern
		message = message.replaceFirst("\r", "");

		// there *may* be another line separator after the pattern
		int index = ex.getIndex();
		String pattern = ex.getPattern();
		if (index >= 0 && pattern != null && index < pattern.length()) {
			int last = message.lastIndexOf("\r");
			message = message.substring(0, last) + message.substring(last + 1);
		}
		return message;
	}
}
