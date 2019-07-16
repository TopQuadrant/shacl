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
package org.topbraid.shacl.expr.lib;

import java.util.Collections;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.topbraid.shacl.expr.AbstractNodeExpression;
import org.topbraid.shacl.expr.AtomicNodeExpression;
import org.topbraid.shacl.expr.NodeExpressionContext;
import org.topbraid.shacl.expr.NodeExpressionVisitor;

public class ConstantTermExpression extends AbstractNodeExpression implements AtomicNodeExpression {
	
	public ConstantTermExpression(RDFNode term) {
		super(term);
	}


	@Override
	public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		return WrappedIterator.create(Collections.singletonList(getRDFNode()).iterator());
	}

	
	@Override
	public String getFunctionalSyntax() {
		return FmtUtils.stringForNode(getRDFNode().asNode(), getRDFNode().getModel());
	}
	
	
	@Override
	public String getTypeId() {
		return "constant";
	}


	@Override
	public String toString() {
		return FmtUtils.stringForRDFNode(getRDFNode());
	}
	
	
	@Override
	public void visit(NodeExpressionVisitor visitor) {
		visitor.visit(this);
	}
}
