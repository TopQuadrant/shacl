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

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.vocabulary.SH;

public class NodeExpressionFactory {

	private static NodeExpressionFactory singleton = new NodeExpressionFactory();
	
	public static NodeExpressionFactory get() {
		return singleton;
	}
	
	public static void set(NodeExpressionFactory value) {
		singleton = value;
	}
	
	
	public NodeExpression create(RDFNode node) {
		if(SH.this_.equals(node)) {
			return new FocusNodeExpression();
		}
		else if(node.isURIResource() || node.isLiteral()) {
			return new ConstantTermExpression(node);
		}
		else {
			Resource resource = node.asResource();
			Resource filterShape = JenaUtil.getResourceProperty(resource, SH.filterShape);
			Statement nodesS = resource.getProperty(SH.nodes);
			if(filterShape != null && nodesS != null) {
				NodeExpression nodes = create(nodesS.getObject());
				return new FilterShapeExpression(nodes, filterShape);
			}
			else {
				Resource path = JenaUtil.getResourceProperty(resource, SH.path);
				if(path != null) {
					NodeExpression nodes;
					if(nodesS != null) {
						nodes = create(nodesS.getObject());
					}
					else {
						nodes = null;
					}
					return new PathExpression(path, nodes);
				}
				else {
					Resource union = JenaUtil.getResourceProperty(resource, SH.union);
					if(union != null) {
						List<NodeExpression> inputs = new LinkedList<NodeExpression>();
						RDFList list = union.as(RDFList.class);
						for(RDFNode member : list.iterator().toList()) {
							inputs.add(create(member));
						}
						return new UnionExpression(inputs);
					}
					else {
						Resource intersection = JenaUtil.getResourceProperty(resource, SH.intersection);
						if(intersection != null) {
							List<NodeExpression> inputs = new LinkedList<NodeExpression>();
							RDFList list = intersection.as(RDFList.class);
							for(RDFNode member : list.iterator().toList()) {
								inputs.add(create(member));
							}
							return new IntersectionExpression(inputs);
						}
						else {
							Statement s = getFunctionStatement(resource);
							if(s != null) {
								List<NodeExpression> args = new LinkedList<>();
								RDFList list = s.getResource().as(RDFList.class);
								for(RDFNode member : list.iterator().toList()) {
									args.add(create(member));
								}
								return new FunctionExpression(s.getPredicate(), args);
							}
							else {
								throw new IllegalArgumentException("Malformed SHACL node expression");
							}
						}
					}
				}
			}
		}
	}

	
	public Statement getFunctionStatement(Resource resource) {
		for(Statement sc : resource.listProperties().toList()) {
			if(RDF.nil.equals(sc.getObject()) || (sc.getObject().isAnon() && sc.getResource().hasProperty(RDF.first))) {
				return sc;
			}
		}
		return null;
	}
}
