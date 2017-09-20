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
package org.topbraid.shacl.expr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.path.Path;
import org.topbraid.shacl.arq.SHACLPaths;

public class PathExpression extends ComplexNodeExpression {
	
	private NodeExpression input;
	
	private Path jenaPath;
	
	private Resource path;
	
	
	public PathExpression(Resource path, NodeExpression input) {
		this.input = input;
		this.path = path;
		if(path.isAnon()) {
			jenaPath = (Path) SHACLPaths.getJenaPath(SHACLPaths.getPathString(path), path.getModel());
		}
	}

	
	@Override
	public void appendLabel(AppendContext context, String targetVarName) {
		if(input instanceof ComplexNodeExpression) {
			String varName = context.getNextVarName();
			((ComplexNodeExpression)input).appendLabel(context, varName);
			context.indent();
			context.append("?" + varName);
			context.append(" ");
			context.append(SHACLPaths.getPathString(path));
			context.append(" ");
			context.append("?" + targetVarName);
		}
		else {
			context.indent();
			if(input instanceof AtomicNodeExpression) {
				context.append(input.toString());
			}
			else {
				context.append("$this");
			}
			context.append(" ");
			context.append(SHACLPaths.getPathString(path));
			context.append(" ");
			context.append("?" + targetVarName);
		}
		context.append(" .\n");
	}


	@Override
	public List<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		if(input != null) {
			Set<RDFNode> results = new HashSet<>();
			if(jenaPath == null) {
				for(RDFNode node : input.eval(focusNode, context)) {
					SHACLPaths.addValueNodes(node.inModel(context.getDataset().getDefaultModel()), path, results);
				}
			}
			else {
				for(RDFNode node : input.eval(focusNode, context)) {
					SHACLPaths.addValueNodes(node.inModel(context.getDataset().getDefaultModel()), jenaPath, results);
				}
			}
			return new ArrayList<RDFNode>(results);
		}
		else {
			List<RDFNode> results = new LinkedList<>();
			if(jenaPath == null) {
				SHACLPaths.addValueNodes(focusNode.inModel(context.getDataset().getDefaultModel()), path, results);
			}
			else {
				SHACLPaths.addValueNodes(focusNode.inModel(context.getDataset().getDefaultModel()), jenaPath, results);
			}
			return results;
		}
	}
}
