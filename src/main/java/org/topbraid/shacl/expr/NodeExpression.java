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

import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Interface of all SHACL node expression runtime objects.
 * 
 * @author Holger Knublauch
 */
public interface NodeExpression {

	/**
	 * Produces an iterator of value nodes for a given focus node.
	 * @param focusNode  the focus node
	 * @param context  the context
	 * @return an iterator (never null)
	 */
	ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context);
	
	
	/**
	 * Produces an iterator of focus nodes for a given value node.
	 * This operation can only be called for node expressions where isReversible returns true
	 * and may throw an IllegalStateException otherwise.
	 * @param valueNode  the value node
	 * @param context  the context
	 * @return an iterator (never null)
	 */
	ExtendedIterator<RDFNode> evalReverse(RDFNode valueNode, NodeExpressionContext context);
	
	
	/**
	 * Produces a "functional" syntax used to display node expressions in a compact form.
	 * @return the functional syntax for this expression
	 */
	String getFunctionalSyntax();
	
	
	List<NodeExpression> getInputExpressions();
	
	
	/**
	 * Gets the shape of the output data (if known), based on a context shape.
	 * For example, for a path expression it would try to infer the output shape from the sh:class or sh:node
	 * of the property (path).
	 * @param contextShape  the shape where the evaluation would start, e.g. the node shape of the sh:values statement
	 * @return an output shape or null if unknown
	 */
	Resource getOutputShape(Resource contextShape);
	
	
	/**
	 * Gets the RDF node that was used to construct the expression, in the SHACL node expressions syntax.
	 * @return the RDF node
	 */
	RDFNode getRDFNode();
	
	
	String getTypeId();
	
	
	/**
	 * Checks if this expression could be evaluated in the reverse direction, with a value given and
	 * then finding all focus nodes.
	 * The function evalReverse can then be called.
	 * NOTE THAT THIS IS CURRENTLY BARELY IMPLEMENTED.
	 * @param context  the context
	 * @return true if reversible
	 */
	boolean isReversible(NodeExpressionContext context);
	
	
	void visit(NodeExpressionVisitor visitor);
}
