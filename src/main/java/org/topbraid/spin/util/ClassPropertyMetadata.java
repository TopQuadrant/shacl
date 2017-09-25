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
package org.topbraid.spin.util;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.shacl.vocabulary.TOSH;
import org.topbraid.spin.vocabulary.ARG;
import org.topbraid.spin.vocabulary.SPIN;
import org.topbraid.spin.vocabulary.SPL;

/**
 * Metadata about a property at a given class.
 * Populated from SHACL constraints, OWL restrictions and SPIN constraints.
 * 
 * @author Holger Knublauch
 */
public class ClassPropertyMetadata {
	
	private Node description;
	
	private Node editWidget;
	
	private Node localRange;
	
	private Integer maxCount;
	
	private Node name;
	
	private Node order;
	
	private Node property;
	
	private Node viewWidget;
	
	
	ClassPropertyMetadata(Node classNode, Node property, Graph graph) {
		
		this.property = property;
		
		// Init from SHACL shapes
		if(SHACLUtil.exists(graph)) {
			if(JenaNodeUtil.isInstanceOf(classNode, SH.Shape.asNode(), graph)) {
				initFromShape(classNode, graph);
			}
			ExtendedIterator<Triple> it = graph.find(null, SH.targetClass.asNode(), classNode);
			while(it.hasNext()) {
				Node shape = it.next().getSubject();
				initFromShape(shape, graph);
			}
		}
		
		initFromOWLClass(classNode, graph);
		initFromSPINConstraints(classNode, graph);
	}
	
	
	public Node getDescription() {
		return description;
	}
	
	
	public Node getEditWidget() {
		return editWidget;
	}
	
	
	public Node getLocalRange() {
		return localRange;
	}
	
	
	public Integer getMaxCount() {
		return maxCount;
	}
	
	
	public Node getName() {
		return name;
	}
	
	
	public Node getOrder() {
		return order;
	}
	
	
	public Node getViewWidget() {
		return viewWidget;
	}
	
	
	private void initFromOWLClass(Node classNode, Graph graph) {
		ExtendedIterator<Triple> it = graph.find(classNode, RDFS.subClassOf.asNode(), Node.ANY);
		while(it.hasNext()) {
			Node superClass = it.next().getObject();
			if(superClass.isBlank() && graph.contains(superClass, OWL.onProperty.asNode(), property)) {
				if(localRange == null) {
					localRange = JenaNodeUtil.getObject(superClass, OWL.allValuesFrom.asNode(), graph);
					if(localRange != null) {
						it.close();
						break;
					}
				}
				if(maxCount == null) {
					Node maxCountNode = JenaNodeUtil.getObject(superClass, OWL.maxCardinality.asNode(), graph);
					if(maxCountNode == null) {
						maxCountNode = JenaNodeUtil.getObject(superClass, OWL.cardinality.asNode(), graph);
					}
					if(maxCountNode != null && maxCountNode.isLiteral()) {
						Object value = maxCountNode.getLiteralValue();
						if(value instanceof Number) {
							maxCount = ((Number) value).intValue();
						}
					}
				}
			}
		}
	}
	
	
	private void initFromShape(Node shape, Graph graph) {
		if(!graph.contains(shape, SH.deactivated.asNode(), JenaDatatypes.TRUE.asNode())) {
			initFromShape(shape, SH.property.asNode(), graph);
			initFromShape(shape, SH.parameter.asNode(), graph);
		}
	}
	
	
	private void initFromShape(Node shape, Node systemPredicate, Graph graph) {
		ExtendedIterator<Triple> it = graph.find(shape, systemPredicate, Node.ANY);
		while(it.hasNext()) {
			Node propertyShape = it.next().getObject();
			if(!propertyShape.isLiteral()) {
				if(graph.contains(propertyShape, SH.path.asNode(), property)) {
					if(!graph.contains(propertyShape, SH.deactivated.asNode(), JenaDatatypes.TRUE.asNode())) {
						if(description == null) {
							description = JenaNodeUtil.getObject(propertyShape, SH.description.asNode(), graph);
						}
						if(editWidget == null) {
							editWidget = JenaNodeUtil.getObject(propertyShape, TOSH.editWidget.asNode(), graph);
						}
						if(localRange == null) {
							localRange = LocalRangeAtClassNativeFunction.walkPropertyShapesHelper(propertyShape, graph);
						}
						if(maxCount == null) {
							Node maxCountNode = JenaNodeUtil.getObject(propertyShape, SH.maxCount.asNode(), graph);
							if(maxCountNode != null && maxCountNode.isLiteral()) {
								Object value = maxCountNode.getLiteralValue();
								if(value instanceof Number) {
									maxCount = ((Number) value).intValue();
								}
							}
						}
						if(name == null) {
							name = JenaNodeUtil.getObject(propertyShape, SH.name.asNode(), graph);
						}
						if(order == null) {
							order = JenaNodeUtil.getObject(propertyShape, SH.order.asNode(), graph);
						}
						if(viewWidget == null) {
							viewWidget = JenaNodeUtil.getObject(propertyShape, TOSH.viewWidget.asNode(), graph);
						}
					}
				}
			}
		}
	}
	
	
	private void initFromSPINConstraints(Node classNode, Graph graph) {
		if(!(graph instanceof OptimizedMultiUnion) || ((OptimizedMultiUnion)graph).getIncludesSPIN()) {
			ExtendedIterator<Triple> it = graph.find(classNode, SPIN.constraint.asNode(), Node.ANY);
			while(it.hasNext()) {
				Node constraint = it.next().getObject();
				if(graph.contains(constraint, SPL.predicate.asNode(), property) &&
						(graph.contains(constraint, RDF.type.asNode(), SPL.Argument.asNode()) ||
						 graph.contains(constraint, RDF.type.asNode(), SPL.Attribute.asNode()))) {
					if(localRange == null) {
						localRange = JenaNodeUtil.getObject(constraint, SPL.valueType.asNode(), graph);
					}
				}
				if(maxCount == null) {
					if(graph.contains(constraint, SPL.predicate.asNode(), property) &&
							graph.contains(constraint, RDF.type.asNode(), SPL.Argument.asNode())) {
						maxCount = 1;
					}
					else if(graph.contains(constraint, ARG.property.asNode(), property)) {
						if(graph.contains(constraint, RDF.type.asNode(), SPL.PrimaryKeyPropertyConstraint.asNode())) {
							maxCount = 1;
						}
					}
				}
			}
		}		
	}
}
