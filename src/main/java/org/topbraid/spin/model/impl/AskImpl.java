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

package org.topbraid.spin.model.impl;

import org.topbraid.spin.model.Ask;
import org.topbraid.spin.model.print.PrintContext;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;


public class AskImpl extends QueryImpl implements Ask {

	public AskImpl(Node node, EnhGraph eh) {
		super(node, eh);
	}
	
	
	@Override
    public void printSPINRDF(PrintContext context) {
		printComment(context);
		printPrefixes(context);
		context.printIndentation(context.getIndentation());
		context.printKeyword("ASK");
		printStringFrom(context);
		context.print(" ");
		if(context.getIndentation() > 0) {
			// Avoid unnecessary whitespace after ASK -> put on extra row
			context.println();
		}
		printWhere(context);
		printValues(context);
	}
}
