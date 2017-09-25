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

import java.util.LinkedList;
import java.util.List;

import org.topbraid.spin.model.Construct;
import org.topbraid.spin.model.TripleTemplate;
import org.topbraid.spin.model.print.PrintContext;
import org.topbraid.spin.vocabulary.SP;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;


public class ConstructImpl extends QueryImpl implements Construct {

	public ConstructImpl(Node node, EnhGraph graph) {
		super(node, graph);
	}

	
	@Override
    public List<TripleTemplate> getTemplates() {
		List<TripleTemplate> results = new LinkedList<TripleTemplate>();
		for(RDFNode next : getList(SP.templates)) {
			if(next != null && next.isResource()) {
				results.add(next.as(TripleTemplate.class));
			}
		}
		return results;
	}


	@Override
    public void printSPINRDF(PrintContext context) {
		printComment(context);
		printPrefixes(context);
		context.printIndentation(context.getIndentation());
		context.printKeyword("CONSTRUCT");
		context.print(" {");
		context.println();
		for(TripleTemplate template : getTemplates()) {
			context.printIndentation(context.getIndentation() + 1);
			template.print(context);
			context.print(" .");
			context.println();
		}
		context.printIndentation(context.getIndentation());
		context.print("}");
		printStringFrom(context);
		context.println();
		printWhere(context);
		printSolutionModifiers(context);
		printValues(context);
	}
}
